package fr.iut.billetterie.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.iut.billetterie.domain.CarteFidelite;

public interface CarteFideliteRepository extends JpaRepository<CarteFidelite, Long> {

    /** Carte active à l'instant donné : achetée au plus tard maintenant et pas encore expirée. */
    Optional<CarteFidelite> findFirstByClientIdAndAcheteeLeLessThanEqualAndExpireLeAfter(
            Long clientId, LocalDateTime acheteeAvant, LocalDateTime expireApres);
}
