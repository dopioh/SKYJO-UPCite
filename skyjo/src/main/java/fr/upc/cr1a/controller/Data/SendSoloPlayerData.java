package fr.upc.cr1a.controller.Data;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Classe qui implemente les methodes de SendAPI pour le mode solo en local
 */
public class SendSoloPlayerData extends SendAPI{
    private final HttpClient client;

    public SendSoloPlayerData(){
        this.client = HttpClient.newHttpClient();
    }
    

    @Override
    public void WinGame(int totalPlayers, int classement, String accessToken){
        try {
            // https://dadroit.com/json-to-string/
            String jsonBody = "{\"totalPlayers\" : \"totalPlayers\",\n \"classementPlayer\" : \"classement\"}";

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(super.urlAPI)).header("Content-Type", "application/json").header("Authorization", "Bearer : " + accessToken).POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("CODE HTTP : " + response.statusCode());
            System.out.println("API REPOND : " + response.body());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void BuyInShop(){}
}