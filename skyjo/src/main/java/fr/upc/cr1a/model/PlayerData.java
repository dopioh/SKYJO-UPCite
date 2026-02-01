package fr.upc.cr1a.model;

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
    private Rank rank; // Faire une enum Rang ? Ou une classe ? String pour le moment 
    private int elo; // c'est quoi exactement ? les rr sur valo ?
    private int xp;
    private int money;
    // les skins sont dans une list de String mais ils auront leur propre classes plus tard
    private ArrayList<String> skins; // tous les skins du jeu et si le joueur l'a, alors l'attribue unlocked du skin est true

    /**
     * Constructeur pour un nouveau joueur
     * 
     * @param id le joueur le crée au si il ne possede pas de compte
     */
    public PlayerData(String id){
        this.id = id;
        this.rank = Rank.IRON; // ou on commence unranked ?
        this.elo = 0;
        this.xp = 0;
        this.money = 0;
        this.skins = null; // a modif plus tard

    }

    // Getter

    public String getID(){
        return this.id;
    }

    public Rank getPlayerRank(){
        return this.rank;
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
}
