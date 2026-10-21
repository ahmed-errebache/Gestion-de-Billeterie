package fr.iut.billetterie.exception;

/** Traduite en HTTP 404. */
public abstract class RessourceIntrouvableException extends BilletterieException {

    protected RessourceIntrouvableException(String code, String message) {
        super(code, message);
    }
}
