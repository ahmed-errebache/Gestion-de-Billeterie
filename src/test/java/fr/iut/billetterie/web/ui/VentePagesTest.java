package fr.iut.billetterie.web.ui;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.iut.billetterie.domain.Adresse;
import fr.iut.billetterie.domain.CarteFidelite;
import fr.iut.billetterie.domain.Client;
import fr.iut.billetterie.exception.CarteDejaActiveException;
import fr.iut.billetterie.exception.JaugeInsuffisanteException;
import fr.iut.billetterie.security.CompteBilletterie;
import fr.iut.billetterie.security.ComptesDemonstration;
import fr.iut.billetterie.security.ControleAccesClient;
import fr.iut.billetterie.security.SecurityConfig;
import fr.iut.billetterie.service.AchatService;
import fr.iut.billetterie.service.CarteFideliteService;
import fr.iut.billetterie.service.ResultatAchat;

/** Pages de connexion, d'achat et d'espace client, avec la vraie configuration de sécurité. */
@WebMvcTest(controllers = {ConnexionPageController.class, VentePageController.class, EspacePageController.class})
@Import({SecurityConfig.class, ComptesDemonstration.class, ControleAccesClient.class})
class VentePagesTest {

    private static final long CLIENT_ID = ComptesDemonstration.CLIENT_ID_DU_COMPTE_CLIENT;
    private static final CompteBilletterie CLIENT = new CompteBilletterie("client", "x",
            AuthorityUtils.createAuthorityList("ROLE_CLIENT"), CLIENT_ID);
    private static final CompteBilletterie PRODUCTEUR = new CompteBilletterie("producteur", "x",
            AuthorityUtils.createAuthorityList("ROLE_PRODUCTEUR"), null);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AchatService achatService;
    @MockitoBean
    private CarteFideliteService carteFideliteService;

    @Test
    void pageDeConnexion_publique() throws Exception {
        mockMvc.perform(get("/connexion"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void connexionParFormulaire_avecUnCompteDeDemonstration() throws Exception {
        mockMvc.perform(formLogin("/connexion").user("client").password("client123"))
                .andExpect(redirectedUrl("/concerts"))
                .andExpect(authenticated().withUsername("client"));
        mockMvc.perform(formLogin("/connexion").user("client").password("faux"))
                .andExpect(redirectedUrl("/connexion?erreur"))
                .andExpect(unauthenticated());
    }

    @Test
    void espace_anonyme_redirigeVersLaConnexion() throws Exception {
        mockMvc.perform(get("/espace")).andExpect(redirectedUrl("/connexion"));
    }

    @Test
    void achat_valide_redirigeVersLaConfirmation() throws Exception {
        when(achatService.acheter(12L, CLIENT_ID, 2)).thenReturn(new ResultatAchat("ACH-0123456789ABCDEF", 12L,
                CLIENT_ID, 2, new BigDecimal("50.00"), new BigDecimal("0.10"), new BigDecimal("90.00"), 48));

        mockMvc.perform(post("/concerts/12/achat").param("quantite", "2").with(user(CLIENT)).with(csrf()))
                .andExpect(redirectedUrl("/achats/confirmation"))
                .andExpect(flash().attributeExists("achat"));
    }

    @Test
    void confirmation_afficheRemiseMontantEtPlacesRestantes() throws Exception {
        ResultatAchat achat = new ResultatAchat("ACH-0123456789ABCDEF", 12L, CLIENT_ID, 2,
                new BigDecimal("50.00"), new BigDecimal("0.10"), new BigDecimal("90.00"), 48);

        mockMvc.perform(get("/achats/confirmation").flashAttr("achat", achat).locale(Locale.FRANCE).with(user(CLIENT)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ACH-0123456789ABCDEF")))
                .andExpect(content().string(containsString("10 %")))
                .andExpect(content().string(containsString("90,00 €")));
    }

    @Test
    void achat_jaugeInsuffisante_retourSurLeConcertAvecMessage() throws Exception {
        when(achatService.acheter(12L, CLIENT_ID, 6)).thenThrow(new JaugeInsuffisanteException(2));

        mockMvc.perform(post("/concerts/12/achat").param("quantite", "6").with(user(CLIENT)).with(csrf()))
                .andExpect(redirectedUrl("/concerts/12"))
                .andExpect(flash().attribute("erreurAchat", "Il ne reste que 2 places pour ce concert"));
    }

    @Test
    void achat_sansJetonCsrf_403() throws Exception {
        mockMvc.perform(post("/concerts/12/achat").param("quantite", "2").with(user(CLIENT)))
                .andExpect(status().isForbidden());
        verify(achatService, never()).acheter(anyLong(), anyLong(), anyInt());
    }

    @Test
    void achat_parUnProducteur_403() throws Exception {
        mockMvc.perform(post("/concerts/12/achat").param("quantite", "2").with(user(PRODUCTEUR)).with(csrf()))
                .andExpect(status().isForbidden());
        verify(achatService, never()).acheter(anyLong(), anyLong(), anyInt());
    }

    @Test
    void achat_anonyme_redirigeVersLaConnexion() throws Exception {
        mockMvc.perform(post("/concerts/12/achat").param("quantite", "2").with(csrf()))
                .andExpect(redirectedUrl("/connexion"));
        verify(achatService, never()).acheter(anyLong(), anyLong(), anyInt());
    }

    @Test
    void espace_sansCarte_proposeLAchat() throws Exception {
        when(carteFideliteService.carteActive(CLIENT_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/espace").with(user(CLIENT)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Acheter une carte de fidélité")));
    }

    @Test
    void espace_avecCarteActive_afficheSaValidite() throws Exception {
        CarteFidelite carte = new CarteFidelite(new Client("Camille Martin", "camille@example.org",
                new Adresse("12 rue des Lilas", "Nancy")), LocalDateTime.of(2026, 10, 21, 14, 0));
        when(carteFideliteService.carteActive(CLIENT_ID)).thenReturn(Optional.of(carte));

        mockMvc.perform(get("/espace").with(user(CLIENT)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("21/10/2027")));
    }

    @Test
    void achatDeCarte_puisCarteDejaActive() throws Exception {
        mockMvc.perform(post("/espace/carte-fidelite").with(user(CLIENT)).with(csrf()))
                .andExpect(redirectedUrl("/espace"))
                .andExpect(flash().attributeExists("succes"));
        verify(carteFideliteService).acheterCarte(CLIENT_ID);

        when(carteFideliteService.acheterCarte(CLIENT_ID)).thenThrow(new CarteDejaActiveException(CLIENT_ID));
        mockMvc.perform(post("/espace/carte-fidelite").with(user(CLIENT)).with(csrf()))
                .andExpect(redirectedUrl("/espace"))
                .andExpect(flash().attributeExists("erreur"));
    }
}
