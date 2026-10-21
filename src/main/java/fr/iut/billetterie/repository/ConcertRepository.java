package fr.iut.billetterie.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.iut.billetterie.domain.Concert;
import jakarta.persistence.LockModeType;

public interface ConcertRepository extends JpaRepository<Concert, Long>, JpaSpecificationExecutor<Concert> {

    /**
     * Lecture du concert avec verrou pessimiste (SELECT ... FOR UPDATE).
     * À appeler dans une transaction, AVANT toute autre lecture, pour sérialiser les ventes d'un même concert.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Concert c where c.id = :id")
    Optional<Concert> findByIdPourVente(@Param("id") Long id);
}
