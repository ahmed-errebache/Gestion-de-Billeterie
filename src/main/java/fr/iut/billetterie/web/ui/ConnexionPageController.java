package fr.iut.billetterie.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Page de connexion ; l'authentification elle-même est traitée par Spring Security (POST /connexion). */
@Controller
public class ConnexionPageController {

    @GetMapping("/connexion")
    public String connexion() {
        return "connexion";
    }
}
