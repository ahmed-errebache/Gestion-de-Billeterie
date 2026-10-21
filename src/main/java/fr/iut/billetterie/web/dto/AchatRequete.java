package fr.iut.billetterie.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Corps de POST /api/achats. Aucun prix n'est accepté : le montant est calculé par le serveur,
 * un champ de prix éventuellement envoyé est ignoré.
 */
public record AchatRequete(
        @NotNull(message = "L'identifiant du concert est obligatoire")
        Long concertId,

        @NotNull(message = "L'identifiant du client est obligatoire")
        Long clientId,

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être comprise entre 1 et 6 billets")
        @Max(value = 6, message = "La quantité doit être comprise entre 1 et 6 billets")
        Integer quantite) {
}
