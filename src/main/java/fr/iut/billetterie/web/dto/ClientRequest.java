package fr.iut.billetterie.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClientRequest(
        @NotBlank @Size(max = 120) String nom,
        @NotBlank @Email @Size(max = 190) String email,
        @NotNull @Valid AdresseDto adresse) {
}
