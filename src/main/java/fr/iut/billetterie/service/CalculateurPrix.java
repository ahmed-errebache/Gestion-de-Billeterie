package fr.iut.billetterie.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import fr.iut.billetterie.domain.CarteFidelite;

/**
 * Règle de prix d'un achat de billets, calculée uniquement côté serveur.
 * Une carte de fidélité active à la date de l'achat accorde 10 % de remise.
 * Les dates viennent du {@link Clock} injecté pour rester testables.
 */
@Component
public class CalculateurPrix {

    public static final BigDecimal TAUX_REMISE_CARTE = new BigDecimal("0.10");
    public static final BigDecimal AUCUNE_REMISE = new BigDecimal("0.00");

    private final Clock clock;

    public CalculateurPrix(Clock clock) {
        this.clock = clock;
    }

    /**
     * Une carte est active maintenant (selon le Clock) si acheteeLe <= maintenant < expireLe ;
     * la règle elle-même est portée par {@link CarteFidelite#estActiveA}. Sans carte, aucune remise.
     */
    public boolean estCarteActive(CarteFidelite carte) {
        return carte != null && carte.estActiveA(LocalDateTime.now(clock));
    }

    public TarifAchat calculer(BigDecimal prixUnitaire, int quantite, boolean carteActive) {
        if (prixUnitaire == null || prixUnitaire.signum() <= 0) {
            throw new IllegalArgumentException("Le prix unitaire doit être positif");
        }
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }
        BigDecimal tauxRemise = carteActive ? TAUX_REMISE_CARTE : AUCUNE_REMISE;
        BigDecimal montantTotal = prixUnitaire
                .multiply(BigDecimal.valueOf(quantite))
                .multiply(BigDecimal.ONE.subtract(tauxRemise))
                .setScale(2, RoundingMode.HALF_UP);
        return new TarifAchat(prixUnitaire.setScale(2, RoundingMode.HALF_UP), quantite, tauxRemise, montantTotal);
    }
}
