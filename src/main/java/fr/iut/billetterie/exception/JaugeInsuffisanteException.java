package fr.iut.billetterie.exception;

public class JaugeInsuffisanteException extends ConflitException {

    public JaugeInsuffisanteException(long placesRestantes) {
        super("JAUGE_INSUFFISANTE", "Il ne reste que " + placesRestantes + " places pour ce concert");
    }
}
