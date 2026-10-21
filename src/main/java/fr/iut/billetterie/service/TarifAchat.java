package fr.iut.billetterie.service;

import java.math.BigDecimal;

/** Résultat du calcul de prix d'un achat : montants arrondis à deux décimales. */
public record TarifAchat(BigDecimal prixUnitaire, int quantite, BigDecimal tauxRemise, BigDecimal montantTotal) {
}
