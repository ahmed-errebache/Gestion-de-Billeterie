package fr.iut.billetterie.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import fr.iut.billetterie.domain.Concert;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

/** Filtres facultatifs du/au/artiste : un critère absent n'ajoute aucune condition. */
final class ConcertSpecifications {

    private ConcertSpecifications() {
    }

    static Specification<Concert> filtrer(LocalDate du, LocalDate au, String artiste) {
        return (root, query, cb) -> {
            // Charge artiste et salle dans la même requête (évite le N+1 au moment du mapping en DTO)
            Join<Object, Object> artisteJoin = (Join<Object, Object>) root.fetch("artiste");
            root.fetch("salle");

            List<Predicate> predicats = new ArrayList<>();
            if (du != null) {
                predicats.add(cb.greaterThanOrEqualTo(root.get("dateHeure"), du.atStartOfDay()));
            }
            if (au != null) {
                predicats.add(cb.lessThan(root.get("dateHeure"), au.plusDays(1).atStartOfDay()));
            }
            if (artiste != null && !artiste.isBlank()) {
                predicats.add(cb.like(cb.lower(artisteJoin.get("nom")), "%" + artiste.trim().toLowerCase() + "%"));
            }
            return cb.and(predicats.toArray(new Predicate[0]));
        };
    }
}
