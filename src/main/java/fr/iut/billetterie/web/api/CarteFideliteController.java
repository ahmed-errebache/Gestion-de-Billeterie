package fr.iut.billetterie.web.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iut.billetterie.service.CarteFideliteService;
import fr.iut.billetterie.web.dto.CarteFideliteResponse;

/**
 * Achat d'une carte de fidélité. L'accès (CLIENT pour son propre client, ou ADMIN)
 * est vérifié par la SecurityConfig sur le {id} du chemin.
 */
@RestController
@RequestMapping("/api/clients/{id}/carte-fidelite")
public class CarteFideliteController {

    private final CarteFideliteService carteFideliteService;

    public CarteFideliteController(CarteFideliteService carteFideliteService) {
        this.carteFideliteService = carteFideliteService;
    }

    @PostMapping
    public ResponseEntity<CarteFideliteResponse> acheterCarte(@PathVariable("id") Long clientId) {
        CarteFideliteResponse creee = CarteFideliteResponse.depuis(carteFideliteService.acheterCarte(clientId));
        return ResponseEntity.created(URI.create("/api/clients/" + clientId + "/carte-fidelite/" + creee.id()))
                .body(creee);
    }
}
