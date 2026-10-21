package fr.iut.billetterie.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Sort;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import fr.iut.billetterie.domain.AchatBillets;
import fr.iut.billetterie.domain.Adresse;
import fr.iut.billetterie.domain.Client;
import fr.iut.billetterie.domain.Concert;
import fr.iut.billetterie.repository.AchatBilletsRepository;
import fr.iut.billetterie.repository.ClientRepository;
import fr.iut.billetterie.repository.ConcertRepository;

/**
 * Tests de la couche JPA sur le vrai moteur MySQL (pas de base embarquée) :
 * recherche filtrée et somme des billets vendus. Docker doit être démarré.
 * Les données de départ viennent de la migration V2.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ConcertRepositoryJpaTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer(DockerImageName.parse("mysql:8.4"));

    @Autowired ConcertRepository concerts;
    @Autowired ClientRepository clients;
    @Autowired AchatBilletsRepository achats;

    @Test
    void filtreParArtiste() {
        List<Concert> trouves = concerts.findAll(ConcertSpecifications.filtrer(null, null, "nova"), Sort.by("dateHeure"));

        assertThat(trouves).hasSize(1);
        assertThat(trouves.get(0).getArtiste().getNom()).isEqualTo("Nova Pulse");
    }

    @Test
    void filtreParPeriode() {
        List<Concert> trouves = concerts.findAll(
                ConcertSpecifications.filtrer(LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 31), null),
                Sort.by("dateHeure"));

        assertThat(trouves).hasSize(1);
        assertThat(trouves.get(0).getTitre()).isEqualTo("Soirée folk intimiste");
    }

    @Test
    void sansFiltreRenvoieTousLesConcertsDeDemo() {
        assertThat(concerts.findAll(ConcertSpecifications.filtrer(null, null, null), Sort.by("dateHeure"))).hasSize(3);
    }

    @Test
    void sommeDesBilletsVendus() {
        Concert concert = concerts.findAll(ConcertSpecifications.filtrer(null, null, "rives"), Sort.by("dateHeure")).get(0);
        Client client = clients.save(new Client("Test Jpa", "test.jpa@example.org", new Adresse("1 rue Test", "Nancy")));

        assertThat(achats.totalVendu(concert.getId())).isZero();

        achats.save(new AchatBillets("ACH-JPA-1", concert, client, 4, new BigDecimal("100.00"), LocalDateTime.now()));
        achats.save(new AchatBillets("ACH-JPA-2", concert, client, 2, new BigDecimal("50.00"), LocalDateTime.now()));

        assertThat(achats.totalVendu(concert.getId())).isEqualTo(6);
    }
}
