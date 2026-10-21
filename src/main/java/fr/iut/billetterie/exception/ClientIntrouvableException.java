package fr.iut.billetterie.exception;

public class ClientIntrouvableException extends RessourceIntrouvableException {

    public ClientIntrouvableException(Long id) {
        super("CLIENT_INTROUVABLE", "Le client " + id + " n'existe pas");
    }
}
