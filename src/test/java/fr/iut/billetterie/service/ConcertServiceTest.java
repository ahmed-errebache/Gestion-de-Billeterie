package fr.iut.billetterie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.iut.billetterie.domain.Artiste;
import fr.iut.billetterie.domain.Concert;
import fr.iut.billetterie.domain.Salle;
import fr.iut.billetterie.exception.ArtisteIntrouvableException;
import fr.iut.billetterie.exception.ConcertIntrouvableException;
import fr.iut.billetterie.exception.DonneesInvalidesException;
import fr.iut.billetterie.repository.AchatBilletsRepository;
import fr.iut.billetterie.repository.ArtisteRepository;
import fr.iut.billetterie.repository.ConcertRepository;
import fr.iut.billetterie.repository.SalleRepository;
import fr.iut.billetterie.web.dto.ConcertDetail;
import fr.iut.billetterie.web.dto.ConcertRequest;

/** Tests unitaires sans Docker : repositories simulés, horloge figée (jamais de sleep). */
@ExtendWith(MockitoExtension.class)
class ConcertServiceTest {

    private static final Clock HORLOGE = Clock.fixed(Instant.parse("2026-10-21T10:00:00Z"), ZoneOffset.UTC);

    @Mock ConcertRepository concerts;
    @Mock ArtisteRepository artistes;
    @Mock SalleRepository salles;
    @Mock AchatBilletsRepository achats;

    ConcertService service;

    @BeforeEach
    void preparer() {
        service = new ConcertService(concerts, artistes, salles, achats, HORLOGE);
    }

    private Concert unConcert() {
        return new Concert("Soirée test", new Artiste("Nova Pulse", "Électro"),
                new Salle("Petit Théâtre", "Nancy", 50),
                LocalDateTime.of(2027, 3, 12, 20, 30), new BigDecimal("25.00"));
    }

    @Test
    void placesRestantesEgalCapaciteMoinsBilletsVendus() {
        when(concerts.findById(1L)).thenReturn(Optional.of(unConcert()));
        when(achats.totalVendu(any())).thenReturn(6L);

        ConcertDetail detail = service.detail(1L);

        assertThat(detail.capacite()).isEqualTo(50);
        assertThat(detail.placesRestantes()).isEqualTo(44);
    }

    @Test
    void concertInconnuLeveUneException() {
        when(concerts.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detail(99L)).isInstanceOf(ConcertIntrouvableException.class);
    }

    @Test
    void periodeInverseeEstRefusee() {
        assertThatThrownBy(() -> service.rechercher(LocalDate.of(2027, 5, 1), LocalDate.of(2027, 4, 1), null))
                .isInstanceOf(DonneesInvalidesException.class);
    }

    @Test
    void concertDansLePasseEstRefuse() {
        when(artistes.findById(1L)).thenReturn(Optional.of(new Artiste("Nova Pulse", "Électro")));
        when(salles.findById(1L)).thenReturn(Optional.of(new Salle("Petit Théâtre", "Nancy", 50)));
        ConcertRequest passe = new ConcertRequest("Trop tard", 1L, 1L,
                LocalDateTime.of(2026, 10, 21, 9, 0), new BigDecimal("20.00"));

        assertThatThrownBy(() -> service.programmer(passe)).isInstanceOf(DonneesInvalidesException.class);
    }

    @Test
    void artisteInconnuLeveUneException() {
        when(artistes.findById(7L)).thenReturn(Optional.empty());
        ConcertRequest demande = new ConcertRequest("Sans artiste", 7L, 1L,
                LocalDateTime.of(2027, 1, 1, 20, 0), new BigDecimal("20.00"));

        assertThatThrownBy(() -> service.programmer(demande)).isInstanceOf(ArtisteIntrouvableException.class);
    }
}
