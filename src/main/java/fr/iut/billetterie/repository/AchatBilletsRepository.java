package fr.iut.billetterie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.iut.billetterie.domain.AchatBillets;

public interface AchatBilletsRepository extends JpaRepository<AchatBillets, Long> {

    /** Somme des billets déjà vendus pour un concert (0 s'il n'y a aucun achat). */
    @Query("select coalesce(sum(a.quantite), 0) from AchatBillets a where a.concert.id = :concertId")
    long totalVendu(@Param("concertId") Long concertId);

    boolean existsByCode(String code);
}
