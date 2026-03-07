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
    
    public abstract void WinGame(); // Ou juste StatsGame et on voit qui a gagné avec un param ? 

    public abstract void BuyInShop(); // fera baisser la money et give un skin au joueur

    // Quoi d'autres comme requetes possible ? 
}
