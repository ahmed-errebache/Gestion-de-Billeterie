package fr.iut.billetterie.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.iut.billetterie.domain.Artiste;

public interface ArtisteRepository extends JpaRepository<Artiste, Long> {

    boolean existsByNomIgnoreCase(String nom);
}
