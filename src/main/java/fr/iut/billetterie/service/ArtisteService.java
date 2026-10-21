package fr.iut.billetterie.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.iut.billetterie.domain.Artiste;
import fr.iut.billetterie.exception.DoublonException;
import fr.iut.billetterie.repository.ArtisteRepository;
import fr.iut.billetterie.web.dto.ArtisteRequest;
import fr.iut.billetterie.web.dto.ArtisteResponse;

@Service
public class ArtisteService {

    private final ArtisteRepository artistes;

    public ArtisteService(ArtisteRepository artistes) {
        this.artistes = artistes;
    }

    @Transactional
    public ArtisteResponse creer(ArtisteRequest demande) {
        String nom = demande.nom().trim();
        if (artistes.existsByNomIgnoreCase(nom)) {
            throw new DoublonException("Un artiste portant ce nom existe déjà");
        }
        Artiste artiste = artistes.save(new Artiste(nom, demande.genreMusical()));
        return new ArtisteResponse(artiste.getId(), artiste.getNom(), artiste.getGenreMusical());
    }
}
