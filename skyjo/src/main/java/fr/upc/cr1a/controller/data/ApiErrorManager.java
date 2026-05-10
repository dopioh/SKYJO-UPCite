package fr.upc.cr1a.controller.data;

import java.net.ConnectException;
import java.net.http.HttpTimeoutException;

/**
 * Centralise les erreurs API afin de les afficher proprement dans les vues Swing.
 */
public final class ApiErrorManager {

    private static volatile String lastUserMessage = null;
    private static volatile String lastContext = null;
    private static volatile int lastStatusCode = -1;

    private ApiErrorManager() {
    }

    public static void clear() {
        lastUserMessage = null;
        lastContext = null;
        lastStatusCode = -1;
    }

    public static void setHttpError(String context, int statusCode, String responseBody) {
        lastContext = context;
        lastStatusCode = statusCode;
        lastUserMessage = mapHttpStatusToMessage(statusCode, context, responseBody);
    }

    public static void setNetworkError(String context, Exception exception) {
        lastContext = context;
        lastStatusCode = -1;

        if (exception instanceof ConnectException) {
            lastUserMessage = "Oups, impossible de joindre le serveur Skyjo. Verifie ta connexion et relance la partie.";
            return;
        }
        if (exception instanceof HttpTimeoutException) {
            lastUserMessage = "Le serveur prend son temps... Reessaie dans quelques secondes.";
            return;
        }
        if (exception instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            lastUserMessage = "Action interrompue. Reessaie quand tu veux.";
            return;
        }

        lastUserMessage = "Petit bug reseau pendant " + context + ". Reessaie dans un instant.";
    }

    public static String getLastUserMessage() {
        return lastUserMessage;
    }

    public static String getLastUserMessageOrDefault(String fallback) {
        return lastUserMessage == null || lastUserMessage.isBlank() ? fallback : lastUserMessage;
    }

    public static int getLastStatusCode() {
        return lastStatusCode;
    }

    public static String getLastContext() {
        return lastContext;
    }

    private static String mapHttpStatusToMessage(int statusCode, String context, String responseBody) {
        String safeContext = context == null ? "l'operation" : context;
        String body = responseBody == null ? "" : responseBody.toLowerCase();

        return switch (statusCode) {
            case 400 -> "Les infos envoyees pour " + safeContext + " ne sont pas valides. Verifie tes champs.";
            case 401 -> "Mauvaise combinaison identifiant/mot de passe. Essaie encore.";
            case 403 -> "Acces bloque pour " + safeContext + ".";
            case 404 -> "On n'a rien trouve pour " + safeContext + ".";
            case 409 -> "Cet email ou ce pseudo est deja pris. Essaie une autre carte d'identite.";
            case 422 -> msgError422(responseBody);
            case 429 -> "Trop de tentatives d'un coup. Fais une petite pause et reessaie.";
            case 500 -> msgError500(responseBody);
            case 502, 503, 504 -> "Serveur indisponible pour l'instant. Reviens dans quelques instants.";
            default -> {
                if (body.contains("password") || body.contains("invalid login")) {
                    yield "Identifiant ou mot de passe incorrect. Retente ta chance.";
                }
                if (body.contains("duplicate") || body.contains("already")) {
                    yield "Cet email ou pseudo est deja utilise.";
                }
                if (body.contains("database") || body.contains("db")) {
                    yield "La base de donnees n'a pas valide le coup. Reessaie dans un instant.";
                }
                yield "Le serveur a renvoye un code " + statusCode + " pendant " + safeContext + ".";
            }
        };
    }

    public static String msgError500(String response){
        if (response != null && response.contains("stats_player_username_key") && response.contains("already exists")) {
            return "  Pseudo déjà pris, veuillez en choisir un autre.";
        }
        return "  Le serveur Skyjo est en train de mélanger ses cartes. \n  Reessaie un peu plus tard.";
    }

    public static String msgError422(String response){
        if (response != null && response.contains("user_already_exists") ) {
            return "  Vous avez déjà un compte avec cette adresse mail. \n  Veuillez vous connecter avec.";
        }

        if ( response != null && response.contains("anonymous_provider_disabled")){
            return "  Le mail ne doit pas être vide.";
        }

        if(response != null && response.contains("error_code\":\"weak_password")){
            return "  Votre mot de passe est trop faible.\n  Il doit faire au minimum 6 caractères.";
        }
        return "  Format Invalide.";
    }
}
