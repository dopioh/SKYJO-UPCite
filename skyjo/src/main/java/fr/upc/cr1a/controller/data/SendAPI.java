package fr.upc.cr1a.controller.data;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import com.google.gson.JsonObject;

import fr.upc.cr1a.model.player.PlayerData;

/**
 * Ancienne classe abstraite qui passe en static 
 * 
 * @author Mei
 */
public class SendAPI {
    protected static String urlAPI = "https://api-render-su0t.onrender.com";

    protected static HttpClient client = HttpClient.newHttpClient();
    
    public static void WinGameSolo(int totalPlayers, int classement,String accessToken){
        try {
            String jsonBody = "{" + "\"totalPlayers\": " + totalPlayers + "," + "\"positionClassement\": " + classement + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAPI + "/WinGameSolo"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken) 
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP WINGAME : " + response.statusCode());
            System.out.println("API REPOND : " + response.body()); // doit return GOOD
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    /**
     * commande API addMoney qui ajoute de l'argent a un utilisateur dans la DB
     */
    public static PlayerData addMoney(String uuid, int amount){
        try {
            String jsonBody = "{" + "\"uuidStr\": \"" + uuid + "\"," + "\"amount\": " + amount + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAPI + "/api/player/addMoney"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP ADDMONEY : " + response.statusCode());
            System.out.println("API REPOND ADDMONEY : " + response.body());

            if (response.statusCode() == 200) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return gson.fromJson(response.body(), PlayerData.class);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * commande API removeMoney qui retire de l'argent a un utilisateur dans la DB
     */
    public static PlayerData removeMoney(String uuid, int amount){
        try {
            String jsonBody = "{" + "\"uuidStr\": \"" + uuid + "\"," + "\"amount\": " + amount + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAPI + "/api/player/removeMoney"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP REMOVEMONEY : " + response.statusCode());
            System.out.println("API REPOND REMOVEMONEY : " + response.body());

            if (response.statusCode() == 200) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return gson.fromJson(response.body(), PlayerData.class);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * commande API addElo qui ajoute de l'elo a un utilisateur dans la DB
     */
    public static PlayerData addElo(String uuid, int amount){
        try {
            String jsonBody = "{" + "\"uuidStr\": \"" + uuid + "\"," + "\"amount\": " + amount + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAPI + "/api/player/addElo"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP ADDELO : " + response.statusCode());
            System.out.println("API REPOND ADDELO : " + response.body());

            if (response.statusCode() == 200) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return gson.fromJson(response.body(), PlayerData.class);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * commande API removeElo qui retire de l'elo a un utilisateur dans la DB
     */
    public static PlayerData removeElo(String uuid, int amount){
        try {
            String jsonBody = "{" + "\"uuidStr\": \"" + uuid + "\"," + "\"amount\": " + amount + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAPI + "/api/player/removeElo"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP REMOVEELO : " + response.statusCode());
            System.out.println("API REPOND REMOVEELO : " + response.body());

            if (response.statusCode() == 200) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return gson.fromJson(response.body(), PlayerData.class);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * commande API addXP qui ajoute de l'xp a un utilisateur dans la DB
     */
    public static PlayerData addXP(String uuid, int amount){
        try {
            String jsonBody = "{" + "\"uuidStr\": \"" + uuid + "\"," + "\"amount\": " + amount + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAPI + "/api/player/addXP"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP ADDXP : " + response.statusCode());
            System.out.println("API REPOND ADDXP : " + response.body());

            if (response.statusCode() == 200) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return gson.fromJson(response.body(), PlayerData.class);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * commande API removeXP qui retire de l'xp a un utilisateur dans la DB
     */
    public static PlayerData removeXP(String uuid, int amount){
        try {
            String jsonBody = "{" + "\"uuidStr\": \"" + uuid + "\"," + "\"amount\": " + amount + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAPI + "/api/player/removeXP"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP REMOVEXP : " + response.statusCode());
            System.out.println("API REPOND REMOVEXP : " + response.body());

            if (response.statusCode() == 200) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return gson.fromJson(response.body(), PlayerData.class);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // Quoi d'autres comme requetes possible ? 

    /**
     * Methode pour creer ou actualiser les stats du player en local
     * Enregistre dans PlayeDataModel avec gson
     */
    public static PlayerData getStats(String token, String uuid){
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlAPI + "/api/player/stats?uuidStr=" + uuid)).header("Content-Type", "application/json").header("Authorization", "Bearer " + token).GET().build();

            System.out.println("REPONSE ?");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            

            if (response.statusCode() == 200){
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                PlayerData playerData = gson.fromJson(response.body(), PlayerData.class);
                System.out.println("Stats recup : ");
                System.out.println("id : " + playerData.getID());
                System.out.println("username : " + playerData.getUsername());
                System.out.println("rank : " + playerData.getPlayerRank());
                System.out.println("elo : " + playerData.getElo());
                System.out.println("xp : " + playerData.getXp());
                System.out.println("money : " + playerData.getMoney());
                // le mail ne s'affiche pas psq on le recupere que temporerement si on se co avec l'username
                return playerData;
            } else {
                System.out.println("Code HTTP HORS 200 : " + response.statusCode());
                System.out.println("Body HORS 200 : " + response.body());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
