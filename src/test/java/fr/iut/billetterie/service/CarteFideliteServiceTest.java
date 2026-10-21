package fr.iut.billetterie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.iut.billetterie.domain.Adresse;
import fr.iut.billetterie.domain.CarteFidelite;
import fr.iut.billetterie.domain.Client;
import fr.iut.billetterie.exception.CarteDejaActiveException;
import fr.iut.billetterie.exception.ClientIntrouvableException;
import fr.iut.billetterie.repository.CarteFideliteRepository;
import fr.iut.billetterie.repository.ClientRepository;

@ExtendWith(MockitoExtension.class)
class CarteFideliteServiceTest {

    private static final Long CLIENT_ID = 1L;
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 10, 21, 14, 0);

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private CarteFideliteRepository carteFideliteRepository;

    private Client client;

    @BeforeEach
    void preparer() {
        client = new Client("Camille Martin", "camille@example.org", new Adresse("12 rue des Lilas", "Nancy"));
    }

    private CarteFideliteService serviceA(LocalDateTime maintenant) {
        Clock horlogeFixe = Clock.fixed(maintenant.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        return new CarteFideliteService(clientRepository, carteFideliteRepository, horlogeFixe);
    }

    private void aucuneCarteActiveA(LocalDateTime maintenant) {
        when(clientRepository.findByIdPourCarte(CLIENT_ID)).thenReturn(Optional.of(client));
        when(carteFideliteRepository.findFirstByClientIdAndAcheteeLeLessThanEqualAndExpireLeAfter(
                CLIENT_ID, maintenant, maintenant)).thenReturn(Optional.empty());
        when(carteFideliteRepository.save(any(CarteFidelite.class))).thenAnswer(appel -> appel.getArgument(0));
    }

    @Test
    void nouvelleCarte_valableExactementUnAn() {
        aucuneCarteActiveA(MAINTENANT);

        CarteFidelite carte = serviceA(MAINTENANT).acheterCarte(CLIENT_ID);

        assertThat(carte.getClient()).isSameAs(client);
        assertThat(carte.getAcheteeLe()).isEqualTo(MAINTENANT);
        assertThat(carte.getExpireLe()).isEqualTo(LocalDateTime.of(2027, 10, 21, 14, 0));
    }

    @Test
    void carteAcheteeUn29Fevrier_expireLe28FevrierSuivant() {
        LocalDateTime jourBissextile = LocalDateTime.of(2028, 2, 29, 10, 0);
        aucuneCarteActiveA(jourBissextile);

        CarteFidelite carte = serviceA(jourBissextile).acheterCarte(CLIENT_ID);

        assertThat(carte.getExpireLe()).isEqualTo(LocalDateTime.of(2029, 2, 28, 10, 0));
    }

    @Test
    void carteDejaActive_refuseeSansEnregistrement() {
        when(clientRepository.findByIdPourCarte(CLIENT_ID)).thenReturn(Optional.of(client));
        when(carteFideliteRepository.findFirstByClientIdAndAcheteeLeLessThanEqualAndExpireLeAfter(
                CLIENT_ID, MAINTENANT, MAINTENANT))
                .thenReturn(Optional.of(new CarteFidelite(client, MAINTENANT.minusMonths(2))));

        assertThatThrownBy(() -> serviceA(MAINTENANT).acheterCarte(CLIENT_ID))
                .isInstanceOf(CarteDejaActiveException.class);
        verify(carteFideliteRepository, never()).save(any());
    }

    @Test
    void clientInexistant_refuseSansEnregistrement() {
        when(clientRepository.findByIdPourCarte(CLIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceA(MAINTENANT).acheterCarte(CLIENT_ID))
                .isInstanceOf(ClientIntrouvableException.class);
        verify(carteFideliteRepository, never()).save(any());
    }

    @Test
    void verrouSurLeClientPrisAvantLaRechercheDeCarte() {
        aucuneCarteActiveA(MAINTENANT);

        serviceA(MAINTENANT).acheterCarte(CLIENT_ID);

        InOrder ordre = inOrder(clientRepository, carteFideliteRepository);
        ordre.verify(clientRepository).findByIdPourCarte(CLIENT_ID);
        ordre.verify(carteFideliteRepository)
                .findFirstByClientIdAndAcheteeLeLessThanEqualAndExpireLeAfter(CLIENT_ID, MAINTENANT, MAINTENANT);
        ordre.verify(carteFideliteRepository).save(any());
        verify(clientRepository, never()).findById(anyLong());
    }
}
