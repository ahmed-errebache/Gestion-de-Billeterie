package fr.iut.billetterie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Client métier. Il n'est volontairement pas confondu avec le compte de sécurité (HTTP Basic). */
@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Embedded
    private Adresse adresse;

    protected Client() {
    }

    public Client(String nom, String email, Adresse adresse) {
        this.nom = nom;
        this.email = email;
        this.adresse = adresse;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public Adresse getAdresse() { return adresse; }
}
