package fr.iut.billetterie.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "concert")
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artiste_id", nullable = false)
    private Artiste artiste;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    protected Concert() {
    }

    public Concert(String titre, Artiste artiste, Salle salle, LocalDateTime dateHeure, BigDecimal prixUnitaire) {
        this.titre = titre;
        this.artiste = artiste;
        this.salle = salle;
        this.dateHeure = dateHeure;
        this.prixUnitaire = prixUnitaire;
    }

    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public Artiste getArtiste() { return artiste; }
    public Salle getSalle() { return salle; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
}
