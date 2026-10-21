package fr.iut.billetterie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.iut.billetterie.domain.AchatBillets;
import fr.iut.billetterie.domain.Adresse;
import fr.iut.billetterie.domain.Artiste;
import fr.iut.billetterie.domain.CarteFidelite;
import fr.iut.billetterie.domain.Client;
import fr.iut.billetterie.domain.Concert;
import fr.iut.billetterie.domain.Salle;
import fr.iut.billetterie.exception.ClientIntrouvableException;
import fr.iut.billetterie.exception.ConcertIntrouvableException;
import fr.iut.billetterie.exception.DonneesInvalidesException;
import fr.iut.billetterie.exception.JaugeInsuffisanteException;
import fr.iut.billetterie.repository.AchatBilletsRepository;
import fr.iut.billetterie.repository.CarteFideliteRepository;
import fr.iut.billetterie.repository.ClientRepository;
import fr.iut.billetterie.repository.ConcertRepository;

@ExtendWith(MockitoExtension.class)
class AchatServiceTest {

    private static final Long CONCERT_ID = 12L;
    private static final Long CLIENT_ID = 7L;
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 10, 21, 14, 0);
    private static final int CAPACITE = 50;

    @Mock
    private ConcertRepository concertRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private AchatBilletsRepository achatBilletsRepository;
    @Mock
    private CarteFideliteRepository carteFideliteRepository;

    private AchatService achatService;
    private Client client;
    private Concert concert;

    @BeforeEach
    void preparer() {
        Clock horlogeFixe = Clock.fixed(MAINTENANT.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        achatService = new AchatService(concertRepository, clientRepository, achatBilletsRepository,
                carteFideliteRepository, new CalculateurPrix(horlogeFixe), horlogeFixe);

        client = new Client("Camille Martin", "camille@example.org", new Adresse("12 rue des Lilas", "Nancy"));
        concert = new Concert("Nova Pulse", new Artiste("Nova Pulse", "Électro"),
                new Salle("Le Petit Théâtre", "Nancy", CAPACITE), MAINTENANT.plusMonths(2), new BigDecimal("50.00"));
    }

    private void concertEtClientExistent(long dejaVendus) {
        when(concertRepository.findByIdPourVente(CONCERT_ID)).thenReturn(Optional.of(concert));
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(achatBilletsRepository.totalVendu(CONCERT_ID)).thenReturn(dejaVendus);
    }

    private void carteTrouvee(CarteFidelite carte) {
        when(carteFideliteRepository.findFirstByClientIdAndAcheteeLeLessThanEqualAndExpireLeAfter(
                CLIENT_ID, MAINTENANT, MAINTENANT)).thenReturn(Optional.ofNullable(carte));
    }

    private void sauvegardeRenvoieLAchat() {
        when(achatBilletsRepository.save(any(AchatBillets.class))).thenAnswer(appel -> appel.getArgument(0));
    }

    @Test
    void sansCarte_prixPleinEtPlacesRestantes() {
        concertEtClientExistent(10);
        carteTrouvee(null);
        sauvegardeRenvoieLAchat();

        ResultatAchat resultat = achatService.acheter(CONCERT_ID, CLIENT_ID, 3);

        assertThat(resultat.quantite()).isEqualTo(3);
        assertThat(resultat.prixUnitaire()).isEqualTo(new BigDecimal("50.00"));
        assertThat(resultat.tauxRemise()).isEqualByComparingTo("0");
        assertThat(resultat.montantTotal()).isEqualTo(new BigDecimal("150.00"));
        assertThat(resultat.placesRestantes()).isEqualTo(CAPACITE - 10 - 3);
        assertThat(resultat.code()).startsWith("ACH-");
    }

    @Test
    void carteActive_billetA50FactureA45() {
        concertEtClientExistent(0);
        carteTrouvee(new CarteFidelite(client, MAINTENANT.minusMonths(1)));
        sauvegardeRenvoieLAchat();

        ResultatAchat resultat = achatService.acheter(CONCERT_ID, CLIENT_ID, 1);

        assertThat(resultat.tauxRemise()).isEqualByComparingTo("0.10");
        assertThat(resultat.montantTotal()).isEqualTo(new BigDecimal("45.00"));
    }

    @Test
    void carteExpiree_aucuneRemise() {
        concertEtClientExistent(0);
        // Même si une carte expirée remontait, la règle de date du domaine la refuse
        carteTrouvee(new CarteFidelite(client, MAINTENANT.minusYears(1).minusDays(1)));
        sauvegardeRenvoieLAchat();

        ResultatAchat resultat = achatService.acheter(CONCERT_ID, CLIENT_ID, 1);

        assertThat(resultat.tauxRemise()).isEqualByComparingTo("0");
        assertThat(resultat.montantTotal()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void montantEnregistreEgalAuMontantCalcule() {
        concertEtClientExistent(0);
        carteTrouvee(new CarteFidelite(client, MAINTENANT.minusMonths(1)));
        sauvegardeRenvoieLAchat();

        achatService.acheter(CONCERT_ID, CLIENT_ID, 2);

        ArgumentCaptor<AchatBillets> achat = ArgumentCaptor.forClass(AchatBillets.class);
        verify(achatBilletsRepository).save(achat.capture());
        assertThat(achat.getValue().getMontantTotal()).isEqualTo(new BigDecimal("90.00"));
        assertThat(achat.getValue().getQuantite()).isEqualTo(2);
        assertThat(achat.getValue().getCreeLe()).isEqualTo(MAINTENANT);
    }

    @Test
    void jaugeInsuffisante_409SansEnregistrement() {
        concertEtClientExistent(CAPACITE - 2);

        assertThatThrownBy(() -> achatService.acheter(CONCERT_ID, CLIENT_ID, 3))
                .isInstanceOf(JaugeInsuffisanteException.class)
                .hasMessage("Il ne reste que 2 places pour ce concert");
        verify(achatBilletsRepository, never()).save(any());
    }

    @Test
    void dernieresPlaces_venduesExactement() {
        concertEtClientExistent(CAPACITE - 4);
        carteTrouvee(null);
        sauvegardeRenvoieLAchat();

        ResultatAchat resultat = achatService.acheter(CONCERT_ID, CLIENT_ID, 4);

        assertThat(resultat.placesRestantes()).isZero();
    }

    @Test
    void verrouSurLeConcertPrisAvantLaSommeVendue() {
        concertEtClientExistent(0);
        carteTrouvee(null);
        sauvegardeRenvoieLAchat();

        achatService.acheter(CONCERT_ID, CLIENT_ID, 1);

        InOrder ordre = inOrder(concertRepository, clientRepository, achatBilletsRepository);
        ordre.verify(concertRepository).findByIdPourVente(CONCERT_ID);
        ordre.verify(clientRepository).findById(CLIENT_ID);
        ordre.verify(achatBilletsRepository).totalVendu(CONCERT_ID);
        ordre.verify(achatBilletsRepository).save(any());
    }

    @Test
    void concertIntrouvable() {
        when(concertRepository.findByIdPourVente(CONCERT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> achatService.acheter(CONCERT_ID, CLIENT_ID, 1))
                .isInstanceOf(ConcertIntrouvableException.class);
        verify(achatBilletsRepository, never()).save(any());
    }

    @Test
    void clientNonInscrit() {
        when(concertRepository.findByIdPourVente(CONCERT_ID)).thenReturn(Optional.of(concert));
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> achatService.acheter(CONCERT_ID, CLIENT_ID, 1))
                .isInstanceOf(ClientIntrouvableException.class);
        verify(achatBilletsRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 7, -1})
    void quantiteHorsDe1a6_refuseeSansToucherLaBase(int quantite) {
        assertThatThrownBy(() -> achatService.acheter(CONCERT_ID, CLIENT_ID, quantite))
                .isInstanceOf(DonneesInvalidesException.class);
        verifyNoInteractions(concertRepository, clientRepository, achatBilletsRepository);
    }
}
