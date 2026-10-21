package fr.iut.billetterie.web.dto;

import java.math.BigDecimal;

import fr.iut.billetterie.service.ResultatAchat;

/** Réponse 201 de POST /api/achats. */
public record AchatResponse(String code, Long concertId, Long clientId, int quantite,
                           BigDecimal prixUnitaire, BigDecimal tauxRemise, BigDecimal montantTotal,
                           long placesRestantes) {

    public static AchatResponse depuis(ResultatAchat resultat) {
        return new AchatResponse(resultat.code(), resultat.concertId(), resultat.clientId(), resultat.quantite(),
                resultat.prixUnitaire(), resultat.tauxRemise(), resultat.montantTotal(), resultat.placesRestantes());
    }
}
