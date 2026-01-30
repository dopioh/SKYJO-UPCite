package fr.upc.cr1a.model;

import fr.upc.cr1a.audio.AudioManager;
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

    public static void main(String[] args) throws Exception {
        new GameLauncher();
        AudioManager audio = new AudioManager();
        audio.playBgm("/music/calmbgm.wav");
        audio.playSfx("/sfx/UIclick.wav");
    }
}
