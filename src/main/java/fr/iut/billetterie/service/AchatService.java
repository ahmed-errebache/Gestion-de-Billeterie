package fr.iut.billetterie.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.iut.billetterie.domain.AchatBillets;
import fr.iut.billetterie.domain.CarteFidelite;
import fr.iut.billetterie.domain.Client;
import fr.iut.billetterie.domain.Concert;
import fr.iut.billetterie.exception.ClientIntrouvableException;
import fr.iut.billetterie.exception.ConcertIntrouvableException;
import fr.iut.billetterie.exception.DonneesInvalidesException;
import fr.iut.billetterie.exception.JaugeInsuffisanteException;
import fr.iut.billetterie.repository.AchatBilletsRepository;
import fr.iut.billetterie.repository.CarteFideliteRepository;
import fr.iut.billetterie.repository.ClientRepository;
import fr.iut.billetterie.repository.ConcertRepository;

/**
 * Vente de billets : une seule transaction, sans jamais dépasser la jauge de la salle.
 *
 * <p>Stratégie de concurrence : verrou pessimiste sur la ligne du concert (SELECT ... FOR UPDATE),
 * pris en tout premier. Deux ventes du même concert, sur deux threads ou deux instances, sont
 * sérialisées par MySQL : la seconde attend la fin de la première et lit alors la somme à jour.
 * Le verrou est pris avant toute autre lecture car, en REPEATABLE READ, une lecture non
 * verrouillante antérieure figerait un instantané et la somme lue ensuite pourrait être périmée.
 * Toute exception annule la transaction : aucun achat partiel n'est enregistré.
 */
@Service
public class AchatService {

    static final int QUANTITE_MIN = 1;
    static final int QUANTITE_MAX = 6;

    private final ConcertRepository concertRepository;
    private final ClientRepository clientRepository;
    private final AchatBilletsRepository achatBilletsRepository;
    private final CarteFideliteRepository carteFideliteRepository;
    private final CalculateurPrix calculateurPrix;
    private final Clock clock;

    public AchatService(ConcertRepository concertRepository, ClientRepository clientRepository,
                        AchatBilletsRepository achatBilletsRepository, CarteFideliteRepository carteFideliteRepository,
                        CalculateurPrix calculateurPrix, Clock clock) {
        this.concertRepository = concertRepository;
        this.clientRepository = clientRepository;
        this.achatBilletsRepository = achatBilletsRepository;
        this.carteFideliteRepository = carteFideliteRepository;
        this.calculateurPrix = calculateurPrix;
        this.clock = clock;
    }

    @Transactional
    public ResultatAchat acheter(Long concertId, Long clientId, int quantite) {
        verifierQuantite(quantite);

        // 1. Verrou sur le concert, en premier
        Concert concert = concertRepository.findByIdPourVente(concertId)
                .orElseThrow(() -> new ConcertIntrouvableException(concertId));

        // 2. Le client doit être inscrit avant tout achat
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientIntrouvableException(clientId));

        // 3. et 4. Jauge : somme déjà vendue lue sous verrou
        long placesRestantes = concert.getSalle().getCapacite() - achatBilletsRepository.totalVendu(concertId);
        if (quantite > placesRestantes) {
            throw new JaugeInsuffisanteException(placesRestantes);
        }

        // 5. Remise si une carte est active à la date de l'achat
        LocalDateTime maintenant = LocalDateTime.now(clock);
        CarteFidelite carte = carteFideliteRepository
                .findFirstByClientIdAndAcheteeLeLessThanEqualAndExpireLeAfter(clientId, maintenant, maintenant)
                .orElse(null);
        TarifAchat tarif = calculateurPrix.calculer(concert.getPrixUnitaire(), quantite,
                calculateurPrix.estCarteActive(carte));

        // 6. Enregistrement
        AchatBillets achat = achatBilletsRepository.save(new AchatBillets(
                genererCode(), concert, client, quantite, tarif.montantTotal(), maintenant));

        return new ResultatAchat(achat.getCode(), concertId, clientId, quantite, tarif.prixUnitaire(),
                tarif.tauxRemise(), tarif.montantTotal(), placesRestantes - quantite);
    }

    private static void verifierQuantite(int quantite) {
        if (quantite < QUANTITE_MIN || quantite > QUANTITE_MAX) {
            throw new DonneesInvalidesException(
                    "La quantité doit être comprise entre " + QUANTITE_MIN + " et " + QUANTITE_MAX + " billets");
        }
    }

    /** Code lisible et unique (UUID aléatoire), garanti en base par la contrainte uk_achat_code. */
    private static String genererCode() {
        return "ACH-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
