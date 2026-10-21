package fr.iut.billetterie.web.ui;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import fr.iut.billetterie.service.ConcertService;

/** Pages publiques : liste filtrable des concerts et fiche détail. */
@Controller
public class ConcertPageController {

    private final ConcertService concerts;

    public ConcertPageController(ConcertService concerts) {
        this.concerts = concerts;
    }

    @GetMapping("/")
    public String accueil() {
        return "redirect:/concerts";
    }

    @GetMapping("/concerts")
    public String liste(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
            @RequestParam(required = false) String artiste,
            Model model) {
        model.addAttribute("concerts", concerts.rechercher(du, au, artiste));
        model.addAttribute("du", du);
        model.addAttribute("au", au);
        model.addAttribute("artiste", artiste);
        return "concerts";
    }

    @GetMapping("/concerts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("concert", concerts.detail(id));
        return "concert";
    }
}
