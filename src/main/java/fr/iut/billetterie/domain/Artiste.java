package fr.iut.billetterie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "artiste")
public class Artiste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nom;

    @Column(name = "genre_musical", length = 80)
    private String genreMusical;

    protected Artiste() {
    }

    public Artiste(String nom, String genreMusical) {
        this.nom = nom;
        this.genreMusical = genreMusical;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getGenreMusical() { return genreMusical; }
}
