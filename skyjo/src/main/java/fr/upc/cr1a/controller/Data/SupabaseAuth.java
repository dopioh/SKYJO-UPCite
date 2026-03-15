package fr.upc.cr1a.controller.Data;

//import java.lang.Thread.State;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//import fr.upc.cr1a.model.GameSession;
//import netscape.javascript.JSObject;

// gson pour recup l'iud que supabase renvoie

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

    private static final HttpClient clientAuth = HttpClient.newHttpClient();

    public static boolean connectPlayerByEmail(String email, String password){ // mettre des booleans apres en type retour
        try {
            String jsonBody = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}";

            String url = SUPABASE_URL + "/auth/v1/token?grant_type=password";

            // requete http à envoyer à la database
            //HttpClient client = HttpClient.newHttpClient(); // creer un client pour envoyer apres une requete depuis celui ci

            // OBLIGER de mettre "application" meme si c'est du texte qui est envoyé 
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").header("apikey",SUPABASE_PUBLIC_KEY).POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = clientAuth.send(request, HttpResponse.BodyHandlers.ofString()); // peut lever une exceptions -> obliger try catch

            if (response.statusCode() != 200){
                System.out.println("CODE HTTP : " + response.statusCode());
                System.out.println("SUPABASE REPONSE : " + response.body());
                return false;
            }

            String id = getIUD(response);
            String accessToken = getAccesToken(response);

            SendAPI.getStats(accessToken, id);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();       
            return false;   
        }
    }

    public static boolean connectPlayerByUsername(String username, String password){
        try {

            String email = getEmailFromUsername(username);

            System.out.println(email);

            if (email == null || email.equals("NON_TROUVE")) {
                System.out.println("Connexion annulee : Pseudo introuvable ou API injoignable.");
                return false;
            }

            boolean connectByMail = connectPlayerByEmail(email, password);
            return connectByMail;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getEmailFromUsername(String username){
        try {
            String url = SendAPI.urlAPI + "/api/player/getEmail?username=" + username;

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            HttpResponse<String> response = clientAuth.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && !response.body().equals("NON_TROUVE")) {
                return response.body(); // L'API renvoie directement l'email en texte clair
            }
            return null;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean createAccount(String email, String username, String password) {
        try {
            String jsonBody = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"data\": {\"username\": \"" + username + "\"}}"; // rajoute l'username en tant que metadata

            String url = SUPABASE_URL + "/auth/v1/signup";

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").header("apikey", SUPABASE_PUBLIC_KEY).POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = clientAuth.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200){
                System.out.println("HTTP CODE PAS 200 : " + response.statusCode());
                System.out.println("REPONSE SUPABASE : " + response.body());
                return false;
            }

            String id = getIUD(response);
            String accessToken = getAccesToken(response);

            SendAPI.getStats(accessToken, id);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getIUD(HttpResponse<String> response){
        if (response.statusCode() != 200) return null;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class); // on le transforme en json pour recuperer la value de i'id

        //String prettyJson = gson.toJson(jsonResponse); //pour afficher en joli json la reponse et recup l'id 
        //System.out.println(prettyJson);

        JsonObject user = jsonResponse.getAsJsonObject("user"); // on peut acceder à la value que d'un objet parent direct
        String playerID = (String) user.get("id").getAsString();

        System.out.println("PLAYER ID : " + playerID); // à stocker sur le jeu du joueur et la db
        return playerID;
    }

    private static String getAccesToken(HttpResponse<String> response){
        if (response.statusCode() != 200) return null;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class); 

        String accessToken = jsonResponse.getAsJsonPrimitive("access_token").getAsString();

        System.out.println(accessToken);
        return accessToken;
    }


   /* Juste pour tester dans le terminal
    
    public static void main(String[] args) { // juste pour tester la connexion 
        //System.out.println("TEST CREATION DE COMPTE : ");
        //createAccount("", "", "");

        //System.out.println("TEST CONNECTION AU COMPTE :");
        //connectPlayerByEmail("", "");

        System.out.println("TEST CONNECTION AU COMPTE PAR USERNAME :");
        connectPlayerByUsername("aza", "MDPSECRET");

        //System.out.println("Une Game Win : ");
        //SendAPI.WinGameSolo(8, 2, GameSession.getAccessToken()); // pour le moment ca ne modifie rien dans la db, mais l'API recoit la requete

        // le code http renvoyé doit etre 200 si tout marche
        
    }
    */
}
