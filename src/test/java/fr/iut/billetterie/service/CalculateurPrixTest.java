package fr.iut.billetterie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import fr.iut.billetterie.domain.Adresse;
import fr.iut.billetterie.domain.CarteFidelite;
import fr.iut.billetterie.domain.Client;

class CalculateurPrixTest {

    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 10, 21, 14, 0);
    private static final BigDecimal PRIX_50 = new BigDecimal("50.00");

    private final Clock horlogeFixe = Clock.fixed(MAINTENANT.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private final CalculateurPrix calculateur = new CalculateurPrix(horlogeFixe);
    private final Client client = new Client("Camille Martin", "camille@example.org", new Adresse("12 rue des Lilas", "Nancy"));

    @Test
    void sansCarte_aucuneRemise() {
        boolean active = calculateur.estCarteActive(null);

        TarifAchat tarif = calculateur.calculer(PRIX_50, 1, active);

        assertThat(active).isFalse();
        assertThat(tarif.tauxRemise()).isEqualByComparingTo("0");
        assertThat(tarif.montantTotal()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void carteActive_billetA50EstFactureA45() {
        boolean active = calculateur.estCarteActive(new CarteFidelite(client, MAINTENANT.minusMonths(3)));

        TarifAchat tarif = calculateur.calculer(PRIX_50, 1, active);

        assertThat(active).isTrue();
        assertThat(tarif.tauxRemise()).isEqualByComparingTo("0.10");
        assertThat(tarif.montantTotal()).isEqualTo(new BigDecimal("45.00"));
    }

    @Test
    void carteExpiree_aucuneRemise() {
        boolean active = calculateur.estCarteActive(new CarteFidelite(client, MAINTENANT.minusYears(2)));

        TarifAchat tarif = calculateur.calculer(PRIX_50, 1, active);

        assertThat(active).isFalse();
        assertThat(tarif.montantTotal()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void carteExpirantExactementMaintenant_nEstPlusActive() {
        assertThat(calculateur.estCarteActive(new CarteFidelite(client, MAINTENANT.minusYears(1)))).isFalse();
    }

    @Test
    void carteAcheteeExactementMaintenant_estActive() {
        assertThat(calculateur.estCarteActive(new CarteFidelite(client, MAINTENANT))).isTrue();
    }

    @Test
    void plusieursBilletsAvecRemise_montantArrondiADeuxDecimales() {
        TarifAchat tarif = calculateur.calculer(new BigDecimal("33.33"), 3, true);

        // 33.33 x 3 = 99.99 ; x 0.90 = 89.991 -> 89.99
        assertThat(tarif.montantTotal()).isEqualTo(new BigDecimal("89.99"));
        assertThat(tarif.quantite()).isEqualTo(3);
    }

    @Test
    void prixNulRefuse() {
        assertThatThrownBy(() -> calculateur.calculer(BigDecimal.ZERO, 1, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
