package fr.iut.billetterie.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalleRequest(
        @NotBlank @Size(max = 120) String nom,
        @NotBlank @Size(max = 80) String ville,
        @NotNull @Min(50) @Max(50000) Integer capacite) {
}
