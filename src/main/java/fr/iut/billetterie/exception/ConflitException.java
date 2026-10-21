package fr.iut.billetterie.exception;

/** Traduite en HTTP 409. */
public abstract class ConflitException extends BilletterieException {

    protected ConflitException(String code, String message) {
        super(code, message);
    }
}
