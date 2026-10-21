package fr.iut.billetterie.service;

import java.math.BigDecimal;

/** Résultat d'une vente réussie, tel que le service le transmet à la couche web. */
public record ResultatAchat(String code, Long concertId, Long clientId, int quantite,
                            BigDecimal prixUnitaire, BigDecimal tauxRemise, BigDecimal montantTotal,
                            long placesRestantes) {
}
