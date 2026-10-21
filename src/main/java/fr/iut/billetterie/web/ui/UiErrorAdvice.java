package fr.iut.billetterie.web.ui;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import fr.iut.billetterie.exception.BilletterieException;
import fr.iut.billetterie.exception.ConflitException;
import fr.iut.billetterie.exception.DonneesInvalidesException;
import fr.iut.billetterie.exception.RessourceIntrouvableException;

/** Erreurs affichées en HTML, limitées aux pages (le JSON d'erreur est réservé à l'API). */
@ControllerAdvice(basePackages = "fr.iut.billetterie.web.ui")
public class UiErrorAdvice {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ModelAndView introuvable(RessourceIntrouvableException ex) {
        return page(HttpStatus.NOT_FOUND, "Page introuvable", ex);
    }

    @ExceptionHandler(DonneesInvalidesException.class)
    public ModelAndView invalide(DonneesInvalidesException ex) {
        return page(HttpStatus.BAD_REQUEST, "Demande invalide", ex);
    }

    @ExceptionHandler(ConflitException.class)
    public ModelAndView conflit(ConflitException ex) {
        return page(HttpStatus.CONFLICT, "Opération impossible", ex);
    }

    private ModelAndView page(HttpStatus statut, String titre, BilletterieException ex) {
        ModelAndView vue = new ModelAndView("erreur");
        vue.setStatus(statut);
        vue.addObject("statut", statut.value());
        vue.addObject("titre", titre);
        vue.addObject("message", ex.getMessage());
        return vue;
    }
}
