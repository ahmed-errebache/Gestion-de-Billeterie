package fr.iut.billetterie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "salle", uniqueConstraints = @UniqueConstraint(columnNames = {"nom", "ville"}))
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(nullable = false, length = 80)
    private String ville;

    @Column(nullable = false)
    private int capacite;

    protected Salle() {
    }

    public Salle(String nom, String ville, int capacite) {
        this.nom = nom;
        this.ville = ville;
        this.capacite = capacite;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getVille() { return ville; }
    public int getCapacite() { return capacite; }
}
