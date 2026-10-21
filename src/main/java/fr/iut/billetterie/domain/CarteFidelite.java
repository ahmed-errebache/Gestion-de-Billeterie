package fr.iut.billetterie.domain;

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

/** Carte de fidélité valable exactement un an à partir de son achat. */
@Entity
@Table(name = "carte_fidelite")
public class CarteFidelite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "achetee_le", nullable = false)
    private LocalDateTime acheteeLe;

    @Column(name = "expire_le", nullable = false)
    private LocalDateTime expireLe;

    protected CarteFidelite() {
    }

    public CarteFidelite(Client client, LocalDateTime acheteeLe) {
        this.client = client;
        this.acheteeLe = acheteeLe;
        this.expireLe = acheteeLe.plusYears(1);
    }

    /** Active à l'instant donné : achetée au plus tard maintenant, pas encore expirée. */
    public boolean estActiveA(LocalDateTime instant) {
        return !acheteeLe.isAfter(instant) && expireLe.isAfter(instant);
    }

    public Long getId() { return id; }
    public Client getClient() { return client; }
    public LocalDateTime getAcheteeLe() { return acheteeLe; }
    public LocalDateTime getExpireLe() { return expireLe; }
}
