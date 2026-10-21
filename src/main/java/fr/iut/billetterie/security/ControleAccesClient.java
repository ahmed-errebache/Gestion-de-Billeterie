package fr.iut.billetterie.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Règle « un CLIENT n'agit que pour son propre client, un ADMIN pour tous ».
 * Utilisée par la carte de fidélité (dans SecurityConfig) et par l'achat (clientId du corps).
 * Nom de bean fixé : il est référencé par les expressions @PreAuthorize.
 */
@Component(ControleAccesClient.NOM_BEAN)
public class ControleAccesClient {

    public static final String NOM_BEAN = "controleAccesClient";

    public boolean peutAgirPour(Authentication authentification, Long clientId) {
        if (authentification == null || !authentification.isAuthenticated() || clientId == null) {
            return false;
        }
        if (aLeRole(authentification, "ROLE_ADMIN")) {
            return true;
        }
        return aLeRole(authentification, "ROLE_CLIENT")
                && authentification.getPrincipal() instanceof CompteBilletterie compte
                && clientId.equals(compte.getClientId());
    }

    private static boolean aLeRole(Authentication authentification, String role) {
        return authentification.getAuthorities().stream()
                .anyMatch(autorite -> role.equals(autorite.getAuthority()));
    }
}
