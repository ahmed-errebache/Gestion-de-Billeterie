package fr.iut.billetterie.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.iut.billetterie.domain.Artiste;
import fr.iut.billetterie.domain.Concert;
import fr.iut.billetterie.domain.Salle;
import fr.iut.billetterie.exception.ArtisteIntrouvableException;
import fr.iut.billetterie.exception.ConcertIntrouvableException;
import fr.iut.billetterie.exception.DonneesInvalidesException;
import fr.iut.billetterie.exception.SalleIntrouvableException;
import fr.iut.billetterie.repository.AchatBilletsRepository;
import fr.iut.billetterie.repository.ArtisteRepository;
import fr.iut.billetterie.repository.ConcertRepository;
import fr.iut.billetterie.repository.SalleRepository;
import fr.iut.billetterie.web.dto.ConcertDetail;
import fr.iut.billetterie.web.dto.ConcertRequest;
import fr.iut.billetterie.web.dto.ConcertResume;

@Service
public class ConcertService {

    private final ConcertRepository concerts;
    private final ArtisteRepository artistes;
    private final SalleRepository salles;
    private final AchatBilletsRepository achats;
    private final Clock clock;

    public ConcertService(ConcertRepository concerts, ArtisteRepository artistes, SalleRepository salles,
                          AchatBilletsRepository achats, Clock clock) {
        this.concerts = concerts;
        this.artistes = artistes;
        this.salles = salles;
        this.achats = achats;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ConcertResume> rechercher(LocalDate du, LocalDate au, String artiste) {
        if (du != null && au != null && au.isBefore(du)) {
            throw new DonneesInvalidesException("La date de fin doit être postérieure ou égale à la date de début");
        }
        return concerts.findAll(ConcertSpecifications.filtrer(du, au, artiste), Sort.by("dateHeure"))
                .stream()
                .map(ConcertService::versResume)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConcertDetail detail(Long id) {
        Concert concert = concerts.findById(id).orElseThrow(() -> new ConcertIntrouvableException(id));
        return versDetail(concert, placesRestantes(concert));
    }

    @Transactional
    public ConcertDetail programmer(ConcertRequest demande) {
        Artiste artiste = artistes.findById(demande.artisteId())
                .orElseThrow(() -> new ArtisteIntrouvableException(demande.artisteId()));
        Salle salle = salles.findById(demande.salleId())
                .orElseThrow(() -> new SalleIntrouvableException(demande.salleId()));
        if (!demande.dateHeure().isAfter(LocalDateTime.now(clock))) {
            throw new DonneesInvalidesException("La date du concert doit être dans le futur");
        }
        Concert concert = concerts.save(new Concert(demande.titre().trim(), artiste, salle,
                demande.dateHeure(), demande.prixUnitaire()));
        return versDetail(concert, placesRestantes(concert));
    }

    /** Places restantes = capacité de la salle - somme des billets déjà vendus. */
    public long placesRestantes(Concert concert) {
        return concert.getSalle().getCapacite() - achats.totalVendu(concert.getId());
    }

    private static ConcertResume versResume(Concert c) {
        return new ConcertResume(c.getId(), c.getTitre(), c.getArtiste().getNom(), c.getSalle().getNom(),
                c.getSalle().getVille(), c.getDateHeure(), c.getPrixUnitaire());
    }

    private static ConcertDetail versDetail(Concert c, long placesRestantes) {
        return new ConcertDetail(c.getId(), c.getTitre(), c.getArtiste().getNom(), c.getSalle().getNom(),
                c.getSalle().getVille(), c.getDateHeure(), c.getPrixUnitaire(),
                c.getSalle().getCapacite(), placesRestantes);
    }
}
