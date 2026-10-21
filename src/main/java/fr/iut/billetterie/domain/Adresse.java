package fr.iut.billetterie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Valeur imbriquée dans Client : stockée dans les colonnes adresse_rue et adresse_ville. */
@Embeddable
public class Adresse {

    @Column(name = "adresse_rue", nullable = false, length = 160)
    private String rue;

    @Column(name = "adresse_ville", nullable = false, length = 80)
    private String ville;

    protected Adresse() {
    }

    public Adresse(String rue, String ville) {
        this.rue = rue;
        this.ville = ville;
    }

    public String getRue() { return rue; }
    public String getVille() { return ville; }
}
