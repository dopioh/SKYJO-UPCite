package fr.upc.cr1a.controller.Data;

/**
 * Classe qui redefini les methodes de SendAPI mais pour le mode multijoueur
 * Se fera lors de l'implementation du mode multi - Mei
 */
public class SendMultiPlayerData extends SendAPI{

    @Override
    public void WinGame(int totalPlayers, int classement,String accessToken){}

    @Override
    public void BuyInShop(){}
    
}
