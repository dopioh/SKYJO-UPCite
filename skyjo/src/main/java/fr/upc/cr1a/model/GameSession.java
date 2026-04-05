package fr.upc.cr1a.model;

import fr.upc.cr1a.model.player.PlayerData;

/**
 * Classe pour relier un token a un playerData
 */
public class GameSession {
    private static PlayerData playerData;
    private static String accessToken;
    private static STATUS status;

    public enum STATUS{
        INVITED, // donc sans compte -> playerData et accessToken bloqué à null
        OFFLINE,
        HOST,
        CLIENT
    }

    public static void init(PlayerData Data, String Token, STATUS statusInit){
        accessToken = Token;
        playerData = Data;

        if (!statusInit.equals(STATUS.INVITED) || !statusInit.equals(STATUS.OFFLINE)){
            status = STATUS.INVITED;
            accessToken = null;
            playerData = null;
        } else {
            status = STATUS.OFFLINE;
        }
    }

    public static PlayerData getPlayerData(){
        return playerData;
    }

    public static String getAccessToken(){
        return accessToken;
    }
}
