package fr.iut.billetterie.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.iut.billetterie.service.AchatService;
import fr.iut.billetterie.web.dto.AchatRequest;
import fr.iut.billetterie.web.dto.AchatResponse;
import jakarta.validation.Valid;

/** Vente de billets : le contrôleur valide l'entrée et délègue tout le métier à AchatService. */
@RestController
@RequestMapping("/api/achats")
public class AchatController {

    private final AchatService achatService;

    public AchatController(AchatService achatService) {
        this.achatService = achatService;
    }

    /** Un compte CLIENT n'achète que pour le client métier qui lui est rattaché (403 sinon). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@controleAccesClient.peutAgirPour(authentication, #demande.clientId())")
    public AchatResponse acheter(@Valid @RequestBody AchatRequest demande) {
        return AchatResponse.depuis(achatService.acheter(demande.concertId(), demande.clientId(), demande.quantite()));
    }
}
