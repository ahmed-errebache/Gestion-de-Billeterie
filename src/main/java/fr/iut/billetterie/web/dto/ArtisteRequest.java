package fr.iut.billetterie.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArtisteRequest(
        @NotBlank @Size(max = 120) String nom,
        @Size(max = 80) String genreMusical) {
}
