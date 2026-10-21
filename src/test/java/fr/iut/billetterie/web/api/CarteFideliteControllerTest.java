package fr.iut.billetterie.web.api;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.iut.billetterie.domain.Adresse;
import fr.iut.billetterie.domain.CarteFidelite;
import fr.iut.billetterie.domain.Client;
import fr.iut.billetterie.security.ComptesDemonstration;
import fr.iut.billetterie.security.ControleAccesClient;
import fr.iut.billetterie.security.SecurityConfig;
import fr.iut.billetterie.service.CarteFideliteService;

/** POST /api/clients/{id}/carte-fidelite avec la vraie configuration de sécurité, le service étant simulé. */
@WebMvcTest(CarteFideliteController.class)
@Import({SecurityConfig.class, ComptesDemonstration.class, ControleAccesClient.class})
class CarteFideliteControllerTest {

    private static final long CLIENT_DU_COMPTE = ComptesDemonstration.CLIENT_ID_DU_COMPTE_CLIENT;
    private static final long AUTRE_CLIENT = CLIENT_DU_COMPTE + 1;
    private static final LocalDateTime ACHETEE_LE = LocalDateTime.of(2026, 10, 21, 14, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarteFideliteService carteFideliteService;

    /** Carte telle que la renverrait le service ; l'id serait attribué par la base. */
    private static CarteFidelite carte() {
        return new CarteFidelite(new Client("Camille Martin", "camille@example.org",
                new Adresse("12 rue des Lilas", "Nancy")), ACHETEE_LE);
    }

    @Test
    void clientPourLuiMeme_201AvecDatesEtLocation() throws Exception {
        when(carteFideliteService.acheterCarte(CLIENT_DU_COMPTE)).thenReturn(carte());

        mockMvc.perform(post("/api/clients/{id}/carte-fidelite", CLIENT_DU_COMPTE).with(httpBasic("client", "client123")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.acheteeLe").value("2026-10-21T14:00:00"))
                .andExpect(jsonPath("$.expireLe").value("2027-10-21T14:00:00"));
    }

    @Test
    void adminPourNimporteQuelClient_201() throws Exception {
        when(carteFideliteService.acheterCarte(AUTRE_CLIENT)).thenReturn(carte());

        mockMvc.perform(post("/api/clients/{id}/carte-fidelite", AUTRE_CLIENT).with(httpBasic("admin", "admin123")))
                .andExpect(status().isCreated());
    }

    @Test
    void clientPourUnAutreClient_403() throws Exception {
        mockMvc.perform(post("/api/clients/{id}/carte-fidelite", AUTRE_CLIENT).with(httpBasic("client", "client123")))
                .andExpect(status().isForbidden());
        verify(carteFideliteService, never()).acheterCarte(anyLong());
    }

    @Test
    void producteur_403() throws Exception {
        mockMvc.perform(post("/api/clients/{id}/carte-fidelite", CLIENT_DU_COMPTE)
                        .with(httpBasic("producteur", "producteur123")))
                .andExpect(status().isForbidden());
        verify(carteFideliteService, never()).acheterCarte(anyLong());
    }

    @Test
    void anonyme_401() throws Exception {
        mockMvc.perform(post("/api/clients/{id}/carte-fidelite", CLIENT_DU_COMPTE))
                .andExpect(status().isUnauthorized());
        verify(carteFideliteService, never()).acheterCarte(anyLong());
    }
}
