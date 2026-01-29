package fr.upc.cr1a.model;

import fr.upc.cr1a.view.Frame;

/**
 * Classe pour lancer le jeu 
 * Elle doit servir que pour ca et pas pour autre chose
 */
public class GameLauncher {
    public GameLauncher(){
        // on lance la frame ici 
        new Frame();
    }

    public static void main(String[] args) {
        new GameLauncher();
    }
}
