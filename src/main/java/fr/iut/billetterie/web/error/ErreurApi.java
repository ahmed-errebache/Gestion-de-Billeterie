package fr.iut.billetterie.web.error;

/** Corps JSON stable de toutes les erreurs de l'API. */
public record ErreurApi(String timestamp, int status, String code, String message, String path) {
}
