package fr.iut.billetterie.exception;

/** Règle de validation métier non respectée (traduite en HTTP 400). */
public class DonneesInvalidesException extends BilletterieException {

    public DonneesInvalidesException(String message) {
        super("VALIDATION_ECHOUEE", message);
    }
}
