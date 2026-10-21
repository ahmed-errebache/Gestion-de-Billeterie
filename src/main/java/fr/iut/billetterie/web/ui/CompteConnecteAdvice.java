package fr.iut.billetterie.web.ui;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import fr.iut.billetterie.security.CompteBilletterie;

/** Rend le compte connecté (ou null) disponible dans toutes les pages, pour l'en-tête et les boutons d'achat. */
@ControllerAdvice(basePackages = "fr.iut.billetterie.web.ui")
public class CompteConnecteAdvice {

    @ModelAttribute("compte")
    public CompteBilletterie compte(@AuthenticationPrincipal CompteBilletterie compte) {
        return compte;
    }
}
