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
@Table(name = "achat_billets")
public class AchatBillets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private int quantite;

    @Column(name = "montant_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe;

    protected AchatBillets() {
    }

    public AchatBillets(String code, Concert concert, Client client, int quantite,
                        BigDecimal montantTotal, LocalDateTime creeLe) {
        this.code = code;
        this.concert = concert;
        this.client = client;
        this.quantite = quantite;
        this.montantTotal = montantTotal;
        this.creeLe = creeLe;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public Concert getConcert() { return concert; }
    public Client getClient() { return client; }
    public int getQuantite() { return quantite; }
    public BigDecimal getMontantTotal() { return montantTotal; }
    public LocalDateTime getCreeLe() { return creeLe; }
}
