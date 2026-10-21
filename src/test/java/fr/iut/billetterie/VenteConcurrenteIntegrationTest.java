package fr.iut.billetterie;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import fr.iut.billetterie.exception.JaugeInsuffisanteException;
import fr.iut.billetterie.service.AchatService;
import fr.iut.billetterie.service.ResultatAchat;

/**
 * Scénario obligatoire du sujet, sur MySQL réel : verrou pessimiste et règle de fidélité.
 * Deux threads réels appellent AchatService en même temps, aucune synchronisation en mémoire ni sleep.
 */
@SpringBootTest
@Testcontainers
class VenteConcurrenteIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer(DockerImageName.parse("mysql:8.4"));

    /** Concert de démonstration dans la salle de 50 places, à 25,00 €. */
    private static final long CONCERT_PETITE_SALLE = 1L;
    /** Concert de démonstration à 50,00 €. */
    private static final long CONCERT_A_50_EUROS = 2L;
    private static final int CAPACITE = 50;
    private static final long CLIENT_1 = 1L;
    private static final long CLIENT_2 = 2L;

    @Autowired AchatService achatService;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void repartirDesDonneesDeDemonstration() {
        jdbc.update("delete from achat_billets");
        jdbc.update("delete from carte_fidelite");
    }

    /** Remplit le concert jusqu'à ne laisser que {@code restantes} places (achats de 1 à 6 billets). */
    private void laisserSeulement(int restantes) {
        int aVendre = CAPACITE - restantes;
        int numero = 0;
        while (aVendre > 0) {
            int quantite = Math.min(6, aVendre);
            jdbc.update("insert into achat_billets (code, concert_id, client_id, quantite, montant_total, cree_le) "
                    + "values (?, ?, ?, ?, ?, now())", "PREP-" + numero++, CONCERT_PETITE_SALLE, CLIENT_2,
                    quantite, new BigDecimal("25.00").multiply(BigDecimal.valueOf(quantite)));
            aVendre -= quantite;
        }
    }

    private long totalVendu(long concertId) {
        return jdbc.queryForObject("select coalesce(sum(quantite), 0) from achat_billets where concert_id = ?",
                Long.class, concertId);
    }

    @RepeatedTest(value = 5, name = "Deux achats simultanés de 4 billets pour 5 places : un seul réussit ({currentRepetition}/{totalRepetitions})")
    void deuxAchatsSimultanes_unSeulReussit_aucuneSurvente() throws Exception {
        laisserSeulement(5);
        CountDownLatch depart = new CountDownLatch(1);
        Callable<ResultatAchat> achatClient1 = () -> {
            depart.await();
            return achatService.acheter(CONCERT_PETITE_SALLE, CLIENT_1, 4);
        };
        Callable<ResultatAchat> achatClient2 = () -> {
            depart.await();
            return achatService.acheter(CONCERT_PETITE_SALLE, CLIENT_2, 4);
        };

        List<ResultatAchat> reussis = new ArrayList<>();
        List<Throwable> refus = new ArrayList<>();
        ExecutorService executeur = Executors.newFixedThreadPool(2);
        try {
            List<Future<ResultatAchat>> achats = List.of(executeur.submit(achatClient1), executeur.submit(achatClient2));
            depart.countDown();
            for (Future<ResultatAchat> achat : achats) {
                try {
                    reussis.add(achat.get(30, TimeUnit.SECONDS));
                } catch (ExecutionException e) {
                    refus.add(e.getCause());
                }
            }
        } finally {
            executeur.shutdownNow();
        }

        assertThat(reussis).hasSize(1);
        assertThat(reussis.getFirst().placesRestantes()).isEqualTo(1);
        assertThat(refus).hasSize(1);
        assertThat(refus.getFirst())
                .isInstanceOf(JaugeInsuffisanteException.class)
                .hasMessage("Il ne reste que 1 places pour ce concert");
        assertThat(totalVendu(CONCERT_PETITE_SALLE)).isEqualTo(CAPACITE - 1);
    }

    @Test
    void carteActive_billetA50Facture45_carteExpiree_prixPlein() {
        LocalDateTime maintenant = LocalDateTime.now();
        jdbc.update("insert into carte_fidelite (client_id, achetee_le, expire_le) values (?, ?, ?)",
                CLIENT_1, maintenant.minusMonths(1), maintenant.minusMonths(1).plusYears(1));
        jdbc.update("insert into carte_fidelite (client_id, achetee_le, expire_le) values (?, ?, ?)",
                CLIENT_2, maintenant.minusYears(2), maintenant.minusYears(1));

        ResultatAchat avecCarteActive = achatService.acheter(CONCERT_A_50_EUROS, CLIENT_1, 1);
        ResultatAchat avecCarteExpiree = achatService.acheter(CONCERT_A_50_EUROS, CLIENT_2, 1);

        assertThat(avecCarteActive.montantTotal()).isEqualTo(new BigDecimal("45.00"));
        assertThat(avecCarteActive.tauxRemise()).isEqualByComparingTo("0.10");
        assertThat(avecCarteExpiree.montantTotal()).isEqualTo(new BigDecimal("50.00"));
        assertThat(avecCarteExpiree.tauxRemise()).isEqualByComparingTo("0");
        assertThat(jdbc.queryForObject("select montant_total from achat_billets where code = ?",
                BigDecimal.class, avecCarteActive.code())).isEqualTo(new BigDecimal("45.00"));
    }
}
