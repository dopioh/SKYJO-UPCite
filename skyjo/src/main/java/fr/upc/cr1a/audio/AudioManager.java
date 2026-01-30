package fr.upc.cr1a.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

/**
 * Classe gérant l'audio du jeu, comme la musique de fond (bgm) et les effets sonores (sfx)
 * 
 * @author Hugo
 */
public class AudioManager {
    private Clip bgmClip;

    /**
     * Joue une musique de fond en boucle 
     * @param resourcePath 
     */
    public void playBgm(String resourcePath) {
        stopBgm(); // Si une autre musique de fond est déjà en train d'être jouée

        try {
            URL url = AudioManager.class.getResource(resourcePath);

            if (url == null) throw new RuntimeException("Fichier introuvable");
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                bgmClip = AudioSystem.getClip();
                bgmClip.open(ais);
            }
            
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.err.println("Erreur audio : " + resourcePath);
        }
    }

    /**
     * Stoppe la musique de fond
     */
    public void stopBgm() {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }
}