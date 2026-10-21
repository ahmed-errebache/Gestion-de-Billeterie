package fr.iut.billetterie.web.ui;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Objet lié au formulaire d'inscription (mutable pour th:field). */
public class InscriptionForm {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 120)
    private String nom;

    @NotBlank(message = "L'e-mail est obligatoire")
    @Email(message = "L'e-mail n'est pas valide")
    @Size(max = 190)
    private String email;

    @NotBlank(message = "La rue est obligatoire")
    @Size(max = 160)
    private String rue;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 80)
    private String ville;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRue() { return rue; }
    public void setRue(String rue) { this.rue = rue; }
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
}
