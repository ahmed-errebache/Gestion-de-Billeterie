package fr.iut.billetterie.web.dto;

import java.time.LocalDateTime;

import fr.iut.billetterie.domain.CarteFidelite;

/** Réponse 201 de POST /api/clients/{id}/carte-fidelite. */
public record CarteFideliteResponse(Long id, Long clientId, LocalDateTime acheteeLe, LocalDateTime expireLe) {

    public static CarteFideliteResponse depuis(CarteFidelite carte) {
        return new CarteFideliteResponse(carte.getId(), carte.getClient().getId(),
                carte.getAcheteeLe(), carte.getExpireLe());
    }
}
