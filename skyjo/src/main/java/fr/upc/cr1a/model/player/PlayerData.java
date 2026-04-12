package fr.upc.cr1a.model.player;

import java.util.ArrayList;

/**
 * Classe qui stock les infos du joueur
 * 
 * Tous les attributs ici ne doivent pas pouvoir etre modifié dans le jeu
 * Il seront modifier par une requete serveur sur a base de données
 * 
 * Cette classe ne sert que de facade pour qu'on puisse recuperer/afficher les infos du joueur
 * 
 * @author Mei
 */
public class PlayerData {

    private final String id; // id unique qui permettra de retrouver le player dans la db
    private String username;
    private int rank_id; // Faire une enum Rang ? Ou une classe ? String pour le moment 
    private int elo; // c'est quoi exactement ? les rr sur valo ?
    private int xp;
    private int money;
    private transient boolean isBot; // indique si c'est un bot
    private transient String botType; // type de bot
    // les skins sont dans une list de String mais ils auront leur propre classes plus tard
    private ArrayList<String> skins; // tous les skins du jeu et si le joueur l'a, alors l'attribue unlocked du skin est true // on l'a pas appeler depuis la db encore lui
    private String selectedSkinPath;

    // transient pour dire a gson d'eviter de lire l'attribut

    public PlayerData(String name){
        this.id = null;
        this.username = name;
        this.rank_id = 1; // ou on commence unranked ?
        this.elo = 0;
        this.xp = 0;
        this.money = 1000;
        this.isBot = false;
        this.botType = null;
        this.skins = null; // a modif plus tard
    }

    /**
     * Constructeur que pour le nom, utiliser pour le multijoueur
     */
    public PlayerData(String id, String name){
        this.id = id;
        this.username = name;
        this.rank_id = 1; // ou on commence unranked ?
        this.elo = 0;
        this.xp = 0;
        this.money = 1000;
        this.isBot = false;
        this.botType = null;
        this.skins = null; // a modif plus tard
    }
    
    
    /**
     * Constructeur pour un bot
     * 
     * @param id le nom du bot
     * @param isBot true si c'est un bot
     * @param botType le type de bot
     */
    public PlayerData(String id, boolean isBot, String botType){
        this.id = id;
        this.rank_id = 1;
        this.elo = 0;
        this.xp = 0;
        this.money = 0;
        this.isBot = isBot;
        this.botType = botType;
        this.skins = null;
    }

    // Getter

    public String getID(){
        return this.id;
    }
    
    public boolean isBot(){
        return this.isBot;
    }
    
    public String getBotType(){
        return this.botType;
    }

    public int getRankId(){
        return this.rank_id;
    }

    public int getElo(){
        return this.elo;
    }

    public int getXp(){
        return this.xp;
    }

    public int getMoney(){
        return this.money;
    }

    public ArrayList<String> getListSkinPlayer(){
        return this.skins;
    }

    public String getSelectedSkinPath(){
        return this.selectedSkinPath;
    }

    public String getUsername(){
        return this.username;
    }
}
