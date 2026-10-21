package fr.iut.billetterie.web.ui;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.iut.billetterie.exception.CarteDejaActiveException;
import fr.iut.billetterie.security.CompteBilletterie;
import fr.iut.billetterie.service.CarteFideliteService;

/** Espace du client connecté : état de sa carte de fidélité et achat d'une carte. */
@Controller
public class EspacePageController {

    private final CarteFideliteService carteFideliteService;

    public EspacePageController(CarteFideliteService carteFideliteService) {
        this.carteFideliteService = carteFideliteService;
    }

    @GetMapping("/espace")
    public String espace(@AuthenticationPrincipal CompteBilletterie compte, Model model) {
        model.addAttribute("carte", carteFideliteService.carteActive(compte.getClientId()).orElse(null));
        return "espace";
    }

    @PostMapping("/espace/carte-fidelite")
    public String acheterCarte(@AuthenticationPrincipal CompteBilletterie compte, RedirectAttributes redirection) {
        try {
            carteFideliteService.acheterCarte(compte.getClientId());
            redirection.addFlashAttribute("succes", "Votre carte de fidélité est active pour un an : -10 % sur vos billets.");
        } catch (CarteDejaActiveException e) {
            redirection.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/espace";
    }
}
