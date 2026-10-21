package fr.iut.billetterie.web.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iut.billetterie.service.SalleService;
import fr.iut.billetterie.web.dto.SalleRequest;
import fr.iut.billetterie.web.dto.SalleResponse;
import jakarta.validation.Valid;

/** Opérations d'administration : réservées au rôle ADMIN (règle posée dans la SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final SalleService salles;

    public AdminApiController(SalleService salles) {
        this.salles = salles;
    }

    @PostMapping("/salles")
    public ResponseEntity<SalleResponse> creerSalle(@Valid @RequestBody SalleRequest demande) {
        SalleResponse creee = salles.creer(demande);
        return ResponseEntity.created(URI.create("/api/admin/salles/" + creee.id())).body(creee);
    }
}
