package fr.iut.billetterie.web.error;

import java.time.Clock;
import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import fr.iut.billetterie.exception.ConflitException;
import fr.iut.billetterie.exception.DonneesInvalidesException;
import fr.iut.billetterie.exception.RessourceIntrouvableException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Traduit les erreurs de l'API REST en JSON stable. Limité aux contrôleurs REST pour ne pas
 * toucher aux pages HTML. Volontairement, aucun handler générique sur Exception : les erreurs
 * 401 et 403 restent produites par Spring Security.
 */
@RestControllerAdvice(basePackages = "fr.iut.billetterie.web.api")
public class ApiExceptionHandler {

    private final Clock clock;

    public ApiExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurApi> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(" ; "));
        return reponse(HttpStatus.BAD_REQUEST, "VALIDATION_ECHOUEE",
                message.isEmpty() ? "Données invalides" : message, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurApi> corpsIllisible(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return reponse(HttpStatus.BAD_REQUEST, "VALIDATION_ECHOUEE", "Corps de la requête illisible ou mal formé", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErreurApi> parametreInvalide(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return reponse(HttpStatus.BAD_REQUEST, "VALIDATION_ECHOUEE",
                "Paramètre « " + ex.getName() + " » invalide", req);
    }

    @ExceptionHandler(DonneesInvalidesException.class)
    public ResponseEntity<ErreurApi> donneesInvalides(DonneesInvalidesException ex, HttpServletRequest req) {
        return reponse(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), req);
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurApi> introuvable(RessourceIntrouvableException ex, HttpServletRequest req) {
        return reponse(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), req);
    }

    @ExceptionHandler(ConflitException.class)
    public ResponseEntity<ErreurApi> conflit(ConflitException ex, HttpServletRequest req) {
        return reponse(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), req);
    }

    /** Filet de sécurité si deux requêtes violent en même temps une contrainte d'unicité : aucun détail SQL exposé. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurApi> integrite(DataIntegrityViolationException ex, HttpServletRequest req) {
        return reponse(HttpStatus.CONFLICT, "DOUBLON", "Cette ressource existe déjà ou viole une contrainte", req);
    }

    private ResponseEntity<ErreurApi> reponse(HttpStatus statut, String code, String message, HttpServletRequest req) {
        ErreurApi corps = new ErreurApi(Instant.now(clock).toString(), statut.value(), code, message, req.getRequestURI());
        return ResponseEntity.status(statut).body(corps);
    }
}
