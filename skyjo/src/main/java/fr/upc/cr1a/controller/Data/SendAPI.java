package fr.upc.cr1a.controller.Data;

/**
 * Classe qui regroupe les methodes pour faire des requetes à l'API
 * exp : partie gagné donc augementation des xp etc 
 * 
 * Classe abstract Parent pour diviser la version ENLIGNE du mode solo 
 * 
 * @author Mei
 */
public abstract class SendAPI {
    protected String urlAPI = "https://api-render-su0t.onrender.com";
    
    public abstract void WinGame(int totalPlayers, int classement,String accessToken); // Ou juste StatsGame et on voit qui a gagné avec un param ? 

    public abstract void BuyInShop(); // fera baisser la money et give un skin au joueur -> faudrait la definir ici sans qu'elle soit abstraite 

    // Quoi d'autres comme requetes possible ? 
}
