package fr.iut.billetterie.exception;

public class DoublonException extends ConflitException {

    public DoublonException(String message) {
        super("DOUBLON", message);
    }
}
