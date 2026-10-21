package fr.iut.billetterie.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.iut.billetterie.domain.CarteFidelite;
import fr.iut.billetterie.domain.Client;
import fr.iut.billetterie.exception.CarteDejaActiveException;
import fr.iut.billetterie.exception.ClientIntrouvableException;
import fr.iut.billetterie.repository.CarteFideliteRepository;
import fr.iut.billetterie.repository.ClientRepository;

/**
 * Vente de la carte de fidélité : valable exactement un an, une seule carte active par client.
 *
 * <p>Stratégie de concurrence : verrou pessimiste sur la ligne du client (SELECT ... FOR UPDATE),
 * pris avant la recherche d'une carte active. Deux achats simultanés de carte pour le même client
 * sont sérialisés : le second voit la carte créée par le premier et reçoit un refus.
 */
@Service
public class CarteFideliteService {

    private final ClientRepository clientRepository;
    private final CarteFideliteRepository carteFideliteRepository;
    private final Clock clock;

    public CarteFideliteService(ClientRepository clientRepository, CarteFideliteRepository carteFideliteRepository,
                                Clock clock) {
        this.clientRepository = clientRepository;
        this.carteFideliteRepository = carteFideliteRepository;
        this.clock = clock;
    }

    /** Carte active à l'instant présent (selon le Clock), pour l'afficher au client. */
    @Transactional(readOnly = true)
    public Optional<CarteFidelite> carteActive(Long clientId) {
        LocalDateTime maintenant = LocalDateTime.now(clock);
        return carteFideliteRepository
                .findFirstByClientIdAndAcheteeLeLessThanEqualAndExpireLeAfter(clientId, maintenant, maintenant);
    }

    @Transactional
    public CarteFidelite acheterCarte(Long clientId) {
        // 1. Verrou sur le client, en premier
        Client client = clientRepository.findByIdPourCarte(clientId)
                .orElseThrow(() -> new ClientIntrouvableException(clientId));

        // 2. Une seule carte active à la fois
        LocalDateTime maintenant = LocalDateTime.now(clock);
        if (carteFideliteRepository
                .findFirstByClientIdAndAcheteeLeLessThanEqualAndExpireLeAfter(clientId, maintenant, maintenant)
                .isPresent()) {
            throw new CarteDejaActiveException(clientId);
        }

        // 3. Nouvelle carte : expireLe = acheteeLe + 1 an (fixé par l'entité). Date à la seconde :
        //    la réponse renvoie exactement ce que MySQL stocke (DATETIME(6) tronquerait l'horloge système)
        return carteFideliteRepository.save(new CarteFidelite(client, maintenant.truncatedTo(ChronoUnit.SECONDS)));
    }
}
