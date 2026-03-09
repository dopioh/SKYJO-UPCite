package fr.upc.cr1a.model;

/**
 * Classe pour relier un token a un playerData
 */
public class GameSession {
    private static PlayerData playerData;
    private static String accessToken;

    public static void init(PlayerData Data, String Token){
        accessToken = Token;
        playerData = Data;

    }

    public static PlayerData getPlayerData(){
        return playerData;
    }

    public static String getAccessToken(){
        return accessToken;
    }
}
