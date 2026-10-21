package fr.iut.billetterie.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConcertDetail(
        Long id,
        String titre,
        String artiste,
        String salle,
        String ville,
        LocalDateTime dateHeure,
        BigDecimal prixUnitaire,
        int capacite,
        long placesRestantes) {
}
