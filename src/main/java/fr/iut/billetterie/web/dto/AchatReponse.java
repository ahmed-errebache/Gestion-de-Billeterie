package fr.iut.billetterie.web.dto;

import java.math.BigDecimal;

import fr.iut.billetterie.service.ResultatAchat;

/** Réponse 201 de POST /api/achats. */
public record AchatReponse(String code, Long concertId, Long clientId, int quantite,
                           BigDecimal prixUnitaire, BigDecimal tauxRemise, BigDecimal montantTotal,
                           long placesRestantes) {

    public static AchatReponse depuis(ResultatAchat resultat) {
        return new AchatReponse(resultat.code(), resultat.concertId(), resultat.clientId(), resultat.quantite(),
                resultat.prixUnitaire(), resultat.tauxRemise(), resultat.montantTotal(), resultat.placesRestantes());
    }
}
