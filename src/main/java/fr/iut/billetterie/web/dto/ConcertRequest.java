package fr.iut.billetterie.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** La règle « date future » est contrôlée dans le service avec le Clock injecté (testable). */
public record ConcertRequest(
        @NotBlank @Size(max = 160) String titre,
        @NotNull Long artisteId,
        @NotNull Long salleId,
        @NotNull LocalDateTime dateHeure,
        @NotNull @Positive BigDecimal prixUnitaire) {
}
