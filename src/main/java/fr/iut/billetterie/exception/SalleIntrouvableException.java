package fr.iut.billetterie.exception;

public class SalleIntrouvableException extends RessourceIntrouvableException {

    public SalleIntrouvableException(Long id) {
        super("SALLE_INTROUVABLE", "La salle " + id + " n'existe pas");
    }
}
