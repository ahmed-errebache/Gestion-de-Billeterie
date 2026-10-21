package fr.iut.billetterie.web.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iut.billetterie.service.ClientService;
import fr.iut.billetterie.web.dto.ClientRequest;
import fr.iut.billetterie.web.dto.ClientResponse;
import jakarta.validation.Valid;

/** Inscription publique d'un client (n'ouvre aucun compte de sécurité). */
@RestController
@RequestMapping("/api/clients")
public class ClientApiController {

    private final ClientService clients;

    public ClientApiController(ClientService clients) {
        this.clients = clients;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> inscrire(@Valid @RequestBody ClientRequest demande) {
        ClientResponse cree = clients.inscrire(demande);
        return ResponseEntity.created(URI.create("/api/clients/" + cree.id())).body(cree);
    }
}
