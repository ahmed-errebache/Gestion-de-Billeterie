package fr.iut.billetterie.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.iut.billetterie.domain.Salle;
import fr.iut.billetterie.exception.DoublonException;
import fr.iut.billetterie.repository.SalleRepository;
import fr.iut.billetterie.web.dto.SalleRequest;
import fr.iut.billetterie.web.dto.SalleResponse;

@Service
public class SalleService {

    private final SalleRepository salles;

    public SalleService(SalleRepository salles) {
        this.salles = salles;
    }

    @Transactional
    public SalleResponse creer(SalleRequest demande) {
        String nom = demande.nom().trim();
        String ville = demande.ville().trim();
        if (salles.existsByNomIgnoreCaseAndVilleIgnoreCase(nom, ville)) {
            throw new DoublonException("Une salle de ce nom existe déjà dans cette ville");
        }
        Salle salle = salles.save(new Salle(nom, ville, demande.capacite()));
        return new SalleResponse(salle.getId(), salle.getNom(), salle.getVille(), salle.getCapacite());
    }
}
