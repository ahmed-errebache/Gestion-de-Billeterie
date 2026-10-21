package fr.iut.billetterie.web.ui;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.iut.billetterie.exception.DonneesInvalidesException;
import fr.iut.billetterie.exception.JaugeInsuffisanteException;
import fr.iut.billetterie.security.CompteBilletterie;
import fr.iut.billetterie.service.AchatService;

/**
 * Achat de billets depuis la fiche d'un concert. Le compte connecté achète pour son client rattaché :
 * le client n'est jamais choisi dans le formulaire. Prix, remise et jauge restent dans AchatService.
 */
@Controller
public class VentePageController {

    private final AchatService achatService;

    public VentePageController(AchatService achatService) {
        this.achatService = achatService;
    }

    @PostMapping("/concerts/{id}/achat")
    public String acheter(@PathVariable Long id, @RequestParam int quantite,
                          @AuthenticationPrincipal CompteBilletterie compte, RedirectAttributes redirection) {
        try {
            redirection.addFlashAttribute("achat", achatService.acheter(id, compte.getClientId(), quantite));
            return "redirect:/achats/confirmation";
        } catch (JaugeInsuffisanteException | DonneesInvalidesException e) {
            // Refus attendu : on revient sur la fiche du concert avec un message clair
            redirection.addFlashAttribute("erreurAchat", e.getMessage());
            return "redirect:/concerts/" + id;
        }
    }

    @GetMapping("/achats/confirmation")
    public String confirmation(Model model) {
        if (!model.containsAttribute("achat")) {
            return "redirect:/concerts";
        }
        return "achat-ok";
    }
}
