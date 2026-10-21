package fr.iut.billetterie.web.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iut.billetterie.service.ArtisteService;
import fr.iut.billetterie.service.ConcertService;
import fr.iut.billetterie.web.dto.ArtisteRequest;
import fr.iut.billetterie.web.dto.ArtisteResponse;
import fr.iut.billetterie.web.dto.ConcertDetail;
import fr.iut.billetterie.web.dto.ConcertRequest;
import jakarta.validation.Valid;

/** Opérations de production : réservées aux rôles PRODUCTEUR et ADMIN (règle posée dans la SecurityConfig). */
@RestController
@RequestMapping("/api/production")
public class ProductionApiController {

    private final ArtisteService artistes;
    private final ConcertService concerts;

    public ProductionApiController(ArtisteService artistes, ConcertService concerts) {
        this.artistes = artistes;
        this.concerts = concerts;
    }

    @PostMapping("/artistes")
    public ResponseEntity<ArtisteResponse> creerArtiste(@Valid @RequestBody ArtisteRequest demande) {
        ArtisteResponse cree = artistes.creer(demande);
        return ResponseEntity.created(URI.create("/api/production/artistes/" + cree.id())).body(cree);
    }

    @PostMapping("/concerts")
    public ResponseEntity<ConcertDetail> programmerConcert(@Valid @RequestBody ConcertRequest demande) {
        ConcertDetail cree = concerts.programmer(demande);
        return ResponseEntity.created(URI.create("/api/concerts/" + cree.id())).body(cree);
    }
}
