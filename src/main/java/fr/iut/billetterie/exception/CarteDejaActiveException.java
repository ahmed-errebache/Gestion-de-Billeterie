package fr.iut.billetterie.exception;

public class CarteDejaActiveException extends ConflitException {

    public CarteDejaActiveException(Long clientId) {
        super("CARTE_DEJA_ACTIVE", "Le client " + clientId + " possède déjà une carte de fidélité active");
    }
}
