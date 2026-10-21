package fr.iut.billetterie.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Règles d'accès seules, sans contrôleur ni base : une requête autorisée qui n'atteint
 * aucun contrôleur répond 404, une requête refusée répond 401 ou 403 avant d'y arriver.
 * {@code controllers} désigne une classe qui n'est pas un contrôleur : aucun contrôleur de
 * l'application n'est chargé, le test reste indépendant des endpoints ajoutés ensuite.
 */
@WebMvcTest(controllers = SecurityConfigTest.class)
@Import({SecurityConfig.class, ComptesDemonstration.class, ControleAccesClient.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void lectureDesConcerts_publique() throws Exception {
        mockMvc.perform(get("/api/concerts")).andExpect(status().isNotFound());
    }

    @Test
    void inscriptionClient_publique() throws Exception {
        mockMvc.perform(post("/api/clients")).andExpect(status().isNotFound());
    }

    @Test
    void creationSalle_anonyme_401() throws Exception {
        mockMvc.perform(post("/api/admin/salles")).andExpect(status().isUnauthorized());
    }

    @Test
    void creationSalle_mauvaisMotDePasse_401() throws Exception {
        mockMvc.perform(post("/api/admin/salles").with(httpBasic("admin", "faux")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void creationSalle_client_403() throws Exception {
        mockMvc.perform(post("/api/admin/salles").with(httpBasic("client", "client123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void creationSalle_admin_autorise() throws Exception {
        mockMvc.perform(post("/api/admin/salles").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void creationArtiste_producteur_autorise_client_403() throws Exception {
        mockMvc.perform(post("/api/production/artistes").with(httpBasic("producteur", "producteur123")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/production/artistes").with(httpBasic("client", "client123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void achat_reserveAuClient() throws Exception {
        mockMvc.perform(post("/api/achats").with(httpBasic("client", "client123")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/achats").with(httpBasic("producteur", "producteur123")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/achats")).andExpect(status().isUnauthorized());
    }

    @Test
    void carteFidelite_clientPourLuiMeme_autorise() throws Exception {
        mockMvc.perform(post("/api/clients/{id}/carte-fidelite", ComptesDemonstration.CLIENT_ID_DU_COMPTE_CLIENT)
                        .with(httpBasic("client", "client123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void carteFidelite_clientPourUnAutre_403() throws Exception {
        mockMvc.perform(post("/api/clients/{id}/carte-fidelite", 2).with(httpBasic("client", "client123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void carteFidelite_adminPourToutClient_autorise_producteur_403() throws Exception {
        mockMvc.perform(post("/api/clients/{id}/carte-fidelite", 2).with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/clients/{id}/carte-fidelite", 2).with(httpBasic("producteur", "producteur123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void santeEtDocumentation_publiques() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isNotFound());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
    }

    @Test
    void autresEndpointsActuator_proteges() throws Exception {
        mockMvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
    }

    @Test
    void pagesPubliques_accessiblesSansConnexion() throws Exception {
        mockMvc.perform(get("/concerts")).andExpect(status().isNotFound());
        mockMvc.perform(get("/concerts/1")).andExpect(status().isNotFound());
        mockMvc.perform(get("/inscription")).andExpect(status().isNotFound());
        mockMvc.perform(get("/connexion")).andExpect(status().isNotFound());
    }

    @Test
    void formulaireDInscription_protegeParCsrf() throws Exception {
        mockMvc.perform(post("/inscription")).andExpect(status().isForbidden());
        mockMvc.perform(post("/inscription").with(csrf())).andExpect(status().isNotFound());
    }

    @Test
    void pageProtegee_anonyme_redirigeVersLaConnexion_sansBasic() throws Exception {
        mockMvc.perform(get("/espace"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/connexion"));
        // HTTP Basic ne sert que l'API : il n'ouvre pas les pages
        mockMvc.perform(get("/espace").with(httpBasic("client", "client123")))
                .andExpect(redirectedUrl("/connexion"));
    }
}
