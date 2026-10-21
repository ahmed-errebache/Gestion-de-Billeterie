package fr.iut.billetterie.exception;

public class ConcertIntrouvableException extends RessourceIntrouvableException {

    public ConcertIntrouvableException(Long id) {
        super("CONCERT_INTROUVABLE", "Le concert " + id + " n'existe pas");
    }
}
