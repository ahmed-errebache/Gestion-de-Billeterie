package fr.iut.billetterie.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.iut.billetterie.domain.Salle;

public interface SalleRepository extends JpaRepository<Salle, Long> {

    boolean existsByNomIgnoreCaseAndVilleIgnoreCase(String nom, String ville);
}
