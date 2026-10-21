package fr.iut.billetterie.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.iut.billetterie.exception.DoublonException;
import fr.iut.billetterie.service.ClientService;
import fr.iut.billetterie.web.dto.AdresseDto;
import fr.iut.billetterie.web.dto.ClientRequest;
import fr.iut.billetterie.web.dto.ClientResponse;
import jakarta.validation.Valid;

/** Inscription d'un client depuis le navigateur (n'ouvre aucun compte de sécurité). */
@Controller
public class InscriptionPageController {

    private final ClientService clients;

    public InscriptionPageController(ClientService clients) {
        this.clients = clients;
    }

    @GetMapping("/inscription")
    public String formulaire(Model model) {
        model.addAttribute("form", new InscriptionForm());
        return "inscription";
    }

    @PostMapping("/inscription")
    public String inscrire(@Valid @ModelAttribute("form") InscriptionForm form,
                           BindingResult resultat, RedirectAttributes redirection) {
        if (resultat.hasErrors()) {
            return "inscription";
        }
        try {
            ClientResponse cree = clients.inscrire(new ClientRequest(form.getNom(), form.getEmail(),
                    new AdresseDto(form.getRue(), form.getVille())));
            redirection.addFlashAttribute("client", cree);
            return "redirect:/inscription/confirmation";
        } catch (DoublonException e) {
            resultat.rejectValue("email", "doublon", e.getMessage());
            return "inscription";
        }
    }

    @GetMapping("/inscription/confirmation")
    public String confirmation(Model model) {
        if (!model.containsAttribute("client")) {
            return "redirect:/inscription";
        }
        return "inscription-ok";
    }
}
