package fr.iut.billetterie.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.iut.billetterie.domain.Adresse;
import fr.iut.billetterie.domain.Client;
import fr.iut.billetterie.exception.ClientIntrouvableException;
import fr.iut.billetterie.exception.DoublonException;
import fr.iut.billetterie.repository.ClientRepository;
import fr.iut.billetterie.web.dto.AdresseDto;
import fr.iut.billetterie.web.dto.ClientRequest;
import fr.iut.billetterie.web.dto.ClientResponse;

@Service
public class ClientService {

    private final ClientRepository clients;

    public ClientService(ClientRepository clients) {
        this.clients = clients;
    }

    /** Inscription métier d'un client : ne crée aucun compte de sécurité. */
    @Transactional
    public ClientResponse inscrire(ClientRequest demande) {
        String email = demande.email().trim();
        if (clients.existsByEmailIgnoreCase(email)) {
            throw new DoublonException("Un client avec cet e-mail existe déjà");
        }
        Client client = clients.save(new Client(demande.nom().trim(), email,
                new Adresse(demande.adresse().rue().trim(), demande.adresse().ville().trim())));
        return versReponse(client);
    }

    @Transactional(readOnly = true)
    public Client trouver(Long id) {
        return clients.findById(id).orElseThrow(() -> new ClientIntrouvableException(id));
    }

    private static ClientResponse versReponse(Client c) {
        return new ClientResponse(c.getId(), c.getNom(), c.getEmail(),
                new AdresseDto(c.getAdresse().getRue(), c.getAdresse().getVille()));
    }
}
