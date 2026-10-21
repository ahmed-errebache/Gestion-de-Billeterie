package fr.iut.billetterie.web.dto;

public record ClientResponse(Long id, String nom, String email, AdresseDto adresse) {
}
