package fr.iut.billetterie.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.iut.billetterie.domain.Client;
import jakarta.persistence.LockModeType;

public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByEmailIgnoreCase(String email);

    /**
     * Lecture du client avec verrou pessimiste (SELECT ... FOR UPDATE).
     * Sert à l'achat d'une carte de fidélité : deux requêtes simultanées pour le même client
     * sont sérialisées, donc la règle « une seule carte active » reste vraie sous concurrence.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Client c where c.id = :id")
    Optional<Client> findByIdPourCarte(@Param("id") Long id);
}
