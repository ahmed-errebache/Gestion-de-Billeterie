package fr.iut.billetterie.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConcertResume(
        Long id,
        String titre,
        String artiste,
        String salle,
        String ville,
        LocalDateTime dateHeure,
        BigDecimal prixUnitaire) {
}
