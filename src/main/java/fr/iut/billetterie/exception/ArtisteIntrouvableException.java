package fr.iut.billetterie.exception;

public class ArtisteIntrouvableException extends RessourceIntrouvableException {

    public ArtisteIntrouvableException(Long id) {
        super("ARTISTE_INTROUVABLE", "L'artiste " + id + " n'existe pas");
    }
}
