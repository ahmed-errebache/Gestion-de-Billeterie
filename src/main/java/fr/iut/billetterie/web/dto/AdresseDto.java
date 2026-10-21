package fr.iut.billetterie.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdresseDto(
        @NotBlank @Size(max = 160) String rue,
        @NotBlank @Size(max = 80) String ville) {
}
