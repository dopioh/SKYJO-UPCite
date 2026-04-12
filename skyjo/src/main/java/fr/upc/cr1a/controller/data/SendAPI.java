package fr.upc.cr1a.controller.data;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * commande API buyPack qui retire le prix d'un pack selon son nom cote serveur
     */
    public static PlayerData buyPack(String uuid, String packName){
        try {
            Map<String, String> body = new HashMap<>();
            body.put("uuidStr", uuid);
            body.put("packName", packName);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonBody = gson.toJson(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAPI + "/api/player/buyPack"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP BUYPACK : " + response.statusCode());
            System.out.println("API REPOND BUYPACK : " + response.body());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), PlayerData.class);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * commande API applyGameResult qui applique recompenses/penalites selon resultat et lobby
     */
    public static PlayerData applyGameResult(String uuid, boolean wonGame, List<String> opponents){
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("uuidStr", uuid);
            body.put("result", wonGame ? "win" : "loss");
            body.put("opponents", opponents == null ? new ArrayList<>() : opponents);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonBody = gson.toJson(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAPI + "/api/player/applyGameResult"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP APPLYGAMERESULT : " + response.statusCode());
            System.out.println("API REPOND APPLYGAMERESULT : " + response.body());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), PlayerData.class);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * commande API unlockSkin qui debloque un skin dans la DB
     */
    public static PlayerData unlockSkin(String uuid, String skinPath){
        try {
            Map<String, String> body = new HashMap<>();
            body.put("uuidStr", uuid);
            body.put("skinPath", skinPath);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonBody = gson.toJson(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAPI + "/api/player/unlockSkin"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP UNLOCKSKIN : " + response.statusCode());
            System.out.println("API REPOND UNLOCKSKIN : " + response.body());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), PlayerData.class);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * commande API selectSkin qui equipe un skin dans la DB
     */
    public static PlayerData selectSkin(String uuid, String skinPath){
        try {
            String jsonBody = "{" + "\"uuidStr\": \"" + uuid + "\"," + "\"skinPath\": \"" + skinPath + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAPI + "/api/player/selectSkin"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP SELECTSKIN : " + response.statusCode());
            System.out.println("API REPOND SELECTSKIN : " + response.body());

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
     * recupere tous les joueurs pour le classement
     */
    public static List<PlayerData> getAllPlayers() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAPI + "/api/player/leaderboard"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP LEADERBOARD : " + response.statusCode());
            System.out.println("API REPOND LEADERBOARD : " + response.body());

            if (response.statusCode() == 200) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                PlayerData[] players = gson.fromJson(response.body(), PlayerData[].class);
                return List.of(players);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
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
                System.out.println("rank_id : " + playerData.getRankId());
                System.out.println("elo : " + playerData.getElo());
                System.out.println("xp : " + playerData.getXp());
                System.out.println("money : " + playerData.getMoney());
                System.out.println("skins : " + playerData.getListSkinPlayer());
                System.out.println("selectedSkinPath : " + playerData.getSelectedSkinPath());
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
