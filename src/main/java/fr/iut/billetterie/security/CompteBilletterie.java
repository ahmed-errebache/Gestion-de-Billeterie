package fr.iut.billetterie.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Identité de sécurité, distincte de l'entité Client.
 * Le lien avec un client métier est explicite : {@code clientId} est renseigné
 * pour un compte CLIENT et nul pour PRODUCTEUR ou ADMIN.
 */
public class CompteBilletterie extends User {

    private final Long clientId;

    public CompteBilletterie(String identifiant, String motDePasseHache,
                             Collection<? extends GrantedAuthority> roles, Long clientId) {
        super(identifiant, motDePasseHache, roles);
        this.clientId = clientId;
    }

    public Long getClientId() {
        return clientId;
    }
}
