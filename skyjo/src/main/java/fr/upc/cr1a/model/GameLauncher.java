package fr.upc.cr1a.model;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

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
        URL url = GameLauncher.class.getResource("/sfx/UIclick.wav");
        System.out.println("TEST URL : ----------------------------------------------------" + url); 
        if (url == null) throw new RuntimeException("Fichier introuvable");

        try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
            Thread.sleep(1000); //1000ms pour éviter que le programme se termine avant la fin du son
            clip.close();
        }
    }
}
