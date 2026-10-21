package fr.iut.billetterie.security;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Trois comptes HTTP Basic de démonstration, séparés de l'inscription des clients.
 * Seuls les hachages BCrypt figurent dans le code ; les mots de passe en clair sont dans le README.
 */
@Component
public class ComptesDemonstration implements UserDetailsService {

    /** Client métier rattaché au compte « client » : premier client du jeu de données de démonstration. */
    public static final long CLIENT_ID_DU_COMPTE_CLIENT = 1L;

    private final Map<String, CompteBilletterie> comptes = List.of(
            compte("client", "$2a$10$NuHJhb/ZHwdQG1jrmlAVxevCtzkqJsCIeAIHzAjcj/wBZUF2Zyl0.", "CLIENT", CLIENT_ID_DU_COMPTE_CLIENT),
            compte("producteur", "$2a$10$OiFW2T/WY0jv89rhuiJyJOsCo2KK6Oedy3bYjPN83m4AJWe0HF2ea", "PRODUCTEUR", null),
            compte("admin", "$2a$10$C1KoK7AJN1o7.X.E9/nrx.KDiKT7saRxASQLCnGPkj7jVWKJhLRPm", "ADMIN", null)
    ).stream().collect(Collectors.toUnmodifiableMap(CompteBilletterie::getUsername, Function.identity()));

    @Override
    public UserDetails loadUserByUsername(String identifiant) {
        CompteBilletterie compte = comptes.get(identifiant);
        if (compte == null) {
            throw new UsernameNotFoundException("Compte inconnu");
        }
        // Copie : Spring Security efface le mot de passe de l'instance authentifiée
        return new CompteBilletterie(compte.getUsername(), compte.getPassword(),
                compte.getAuthorities(), compte.getClientId());
    }

    private static CompteBilletterie compte(String identifiant, String hachage, String role, Long clientId) {
        return new CompteBilletterie(identifiant, hachage,
                AuthorityUtils.createAuthorityList("ROLE_" + role), clientId);
    }
}
