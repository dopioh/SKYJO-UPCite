package fr.upc.cr1a.controller.Data;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Classe qui fait la liaison entre le jeu et la base de données
 * SEULEMENT pour qu'un joueur créer ou se connecter à son compte 
 * Ca utilise le systeme d'authentification de Supabase 
 * Pas besoin de chiffrer les mots de passe nous meme dcp
 * Et ca give un ID automatiquement 
 * 
 * @see https://supabase.com/docs/guides/troubleshooting/http-status-codes pour la doc des codes http Supabase
 * 
 * @author Mei
 */
public class SupabaseAuth {

    private static final String SUPABASE_URL = "https://jfystyeubanlvujvlkvw.supabase.co";
    private static final String SUPABASE_PUBLIC_KEY = "sb_publishable_uprBauX8RjU9OFrQamyhLQ_1JYNeA-a"; // clé publique donc aucun soucis de le mettre en dure ici

    public static void connectPlayerByEmail(String email, String password){
        try {
            String jsonBody = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}";

            String url = SUPABASE_URL + "/auth/v1/token?grant_type=password";

            // requete http à envoyer à la database
            HttpClient client = HttpClient.newHttpClient(); // creer un client pour envoyer apres une requete depuis celui ci

            // OBLIGER de mettre "application" meme si c'est du texte qui est envoyé 
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").header("apikey",SUPABASE_PUBLIC_KEY).POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); // peut lever une exceptions -> obliger try catch

            System.out.println("CODE HTTP : " + response.statusCode());
            System.out.println("SUPABASE REPONSE : " + response.body()); // print pour le moment, sera gerer selon le code renvoyer plus tard
            
        } catch (Exception e) {
            e.printStackTrace();          
        }
    }

    public static void connectPlayerByUsername(String username, String password){
        // a definir apres s'etre assuré que par mail ça fonctionne à 100%
    }

    public static void createAccount(String email, String username, String password) {
        try {
            String jsonBody = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"data\": {\"username\": \"" + username + "\"}}"; // rajoute l'username en tant que metadata

            String url = SUPABASE_URL + "/auth/v1/signup";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").header("apikey", SUPABASE_PUBLIC_KEY).POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP CREATION : " + response.statusCode());
            System.out.println("REPONSE SUPABASE : " + response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) { // juste pour tester la connexion 
        //System.out.println("TEST CREATION DE COMPTE : ");
        //createAccount("azaa.projects@gmail.com", "aza", "LESFLEURSSONTJAUNES");

        System.out.println("TEST CONNECTION AU COMPTE :");
        connectPlayerByEmail("azaa.projects@gmail.com", "LESFLEURSSONTJAUNES");

        // le code http renvoyé doit etre 200 si tout marche
        
    }
}
