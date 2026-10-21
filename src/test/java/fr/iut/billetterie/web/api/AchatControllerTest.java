package fr.iut.billetterie.web.api;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import fr.iut.billetterie.security.ComptesDemonstration;
import fr.iut.billetterie.security.ControleAccesClient;
import fr.iut.billetterie.security.SecurityConfig;
import fr.iut.billetterie.service.AchatService;
import fr.iut.billetterie.service.ResultatAchat;

/** POST /api/achats avec la vraie configuration de sécurité, le service métier étant simulé. */
@WebMvcTest(AchatController.class)
@Import({SecurityConfig.class, ComptesDemonstration.class, ControleAccesClient.class})
class AchatControllerTest {

    private static final long CLIENT_DU_COMPTE = ComptesDemonstration.CLIENT_ID_DU_COMPTE_CLIENT;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AchatService achatService;

    private ResultActions acheter(String corps, String identifiant, String motDePasse) throws Exception {
        return mockMvc.perform(post("/api/achats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corps)
                .with(httpBasic(identifiant, motDePasse)));
    }

    private static String corps(long clientId, int quantite) {
        return """
                {"concertId": 12, "clientId": %d, "quantite": %d}
                """.formatted(clientId, quantite);
    }

    @Test
    void achatValide_201AvecLesChampsAttendus() throws Exception {
        when(achatService.acheter(12L, CLIENT_DU_COMPTE, 3)).thenReturn(new ResultatAchat(
                "ACH-0123456789ABCDEF", 12L, CLIENT_DU_COMPTE, 3, new BigDecimal("50.00"),
                new BigDecimal("0.10"), new BigDecimal("135.00"), 42));

        acheter(corps(CLIENT_DU_COMPTE, 3), "client", "client123")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ACH-0123456789ABCDEF"))
                .andExpect(jsonPath("$.quantite").value(3))
                .andExpect(jsonPath("$.prixUnitaire").value(50.00))
                .andExpect(jsonPath("$.tauxRemise").value(0.10))
                .andExpect(jsonPath("$.montantTotal").value(135.00))
                .andExpect(jsonPath("$.placesRestantes").value(42));
    }

    @Test
    void prixEnvoyeParLeClient_ignore() throws Exception {
        when(achatService.acheter(12L, CLIENT_DU_COMPTE, 1)).thenReturn(new ResultatAchat(
                "ACH-0123456789ABCDEF", 12L, CLIENT_DU_COMPTE, 1, new BigDecimal("50.00"),
                new BigDecimal("0.00"), new BigDecimal("50.00"), 10));

        acheter("""
                {"concertId": 12, "clientId": %d, "quantite": 1, "montantTotal": 0.01, "prixUnitaire": 0.01}
                """.formatted(CLIENT_DU_COMPTE), "client", "client123")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.montantTotal").value(50.00));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 7})
    void quantiteHorsDe1a6_400(int quantite) throws Exception {
        acheter(corps(CLIENT_DU_COMPTE, quantite), "client", "client123")
                .andExpect(status().isBadRequest());
        verify(achatService, never()).acheter(anyLong(), anyLong(), anyInt());
    }

    @Test
    void champManquant_400() throws Exception {
        acheter("""
                {"concertId": 12, "quantite": 2}
                """, "client", "client123")
                .andExpect(status().isBadRequest());
        verify(achatService, never()).acheter(anyLong(), anyLong(), anyInt());
    }

    @Test
    void anonyme_401() throws Exception {
        mockMvc.perform(post("/api/achats").contentType(MediaType.APPLICATION_JSON).content(corps(CLIENT_DU_COMPTE, 2)))
                .andExpect(status().isUnauthorized());
        verify(achatService, never()).acheter(anyLong(), anyLong(), anyInt());
    }

    @Test
    void roleInsuffisant_403() throws Exception {
        acheter(corps(CLIENT_DU_COMPTE, 2), "producteur", "producteur123")
                .andExpect(status().isForbidden());
        acheter(corps(CLIENT_DU_COMPTE, 2), "admin", "admin123")
                .andExpect(status().isForbidden());
        verify(achatService, never()).acheter(anyLong(), anyLong(), anyInt());
    }

    @Test
    void clientQuiAchetePourUnAutreClient_403() throws Exception {
        acheter(corps(CLIENT_DU_COMPTE + 1, 2), "client", "client123")
                .andExpect(status().isForbidden());
        verify(achatService, never()).acheter(anyLong(), anyLong(), anyInt());
    }
}
