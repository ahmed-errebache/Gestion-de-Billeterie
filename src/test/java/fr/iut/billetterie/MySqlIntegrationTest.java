package fr.iut.billetterie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import fr.iut.billetterie.domain.Adresse;
import fr.iut.billetterie.domain.Client;
import fr.iut.billetterie.repository.ClientRepository;

/**
 * Trois tests d'intégration sur MySQL réel (Testcontainers + @ServiceConnection).
 * Le simple démarrage du contexte prouve que Flyway construit la base et qu'Hibernate valide le schéma.
 */
@SpringBootTest
@Testcontainers
class MySqlIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer(DockerImageName.parse("mysql:8.4"));

    @Autowired JdbcTemplate jdbc;
    @Autowired ClientRepository clients;
    @Autowired TransactionTemplate transaction;

    @Test
    void migrationsAppliqueesSurUneBaseNeuve() {
        Integer migrations = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success = 1", Integer.class);
        Integer tables = jdbc.queryForObject(
                "select count(*) from information_schema.tables where table_schema = database() "
                        + "and table_name in ('artiste','salle','concert','client','carte_fidelite','achat_billets')",
                Integer.class);

        assertThat(migrations).isGreaterThanOrEqualTo(2);
        assertThat(tables).isEqualTo(6);
        assertThat(jdbc.queryForObject("select count(*) from concert", Integer.class)).isEqualTo(3);
    }

    @Test
    void contraintesDeLaBaseRefusentLesDonneesInvalides() {
        // e-mail unique
        assertThatThrownBy(() -> clients.saveAndFlush(new Client("Doublon",
                "camille.martin@example.org", new Adresse("1 rue X", "Nancy"))))
                .isInstanceOf(DataIntegrityViolationException.class);

        // capacité d'une salle entre 50 et 50 000
        assertThatThrownBy(() -> jdbc.update(
                "insert into salle (nom, ville, capacite) values ('Trop petite', 'Metz', 10)"))
                .isInstanceOf(DataAccessException.class);

        // couple (nom, ville) de salle unique
        assertThatThrownBy(() -> jdbc.update(
                "insert into salle (nom, ville, capacite) values ('Le Petit Théâtre', 'Nancy', 100)"))
                .isInstanceOf(DataAccessException.class);

        // quantité d'un achat entre 1 et 6
        assertThatThrownBy(() -> jdbc.update(
                "insert into achat_billets (code, concert_id, client_id, quantite, montant_total, cree_le) "
                        + "select 'ACH-QTE-7', c.id, cl.id, 7, 10.00, now() from concert c, client cl limit 1"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void uneTransactionEnEchecNeLaisseAucuneEcriturePartielle() {
        long avant = clients.count();

        assertThatThrownBy(() -> transaction.executeWithoutResult(statut -> {
            clients.save(new Client("Fantôme", "fantome@example.org", new Adresse("2 rue Y", "Metz")));
            throw new IllegalStateException("échec volontaire après l'écriture");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(clients.count()).isEqualTo(avant);
        assertThat(clients.existsByEmailIgnoreCase("fantome@example.org")).isFalse();
    }
}
