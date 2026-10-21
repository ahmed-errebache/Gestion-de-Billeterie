package fr.iut.billetterie.exception;

/** Racine des exceptions métier : porte le code stable renvoyé dans le JSON d'erreur. */
public abstract class BilletterieException extends RuntimeException {

    private final String code;

    protected BilletterieException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
