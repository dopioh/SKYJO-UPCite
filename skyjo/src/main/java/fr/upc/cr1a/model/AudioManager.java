package fr.upc.cr1a.model;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

import java.net.URL;

/**
 * Classe gérant l'audio du jeu, comme la musique de fond (bgm) et les effets sonores (sfx)
 * playBgm("resources") : joue une musique de fond en boucle
 * playSfx("resources") : joue un effet sonore une seule fois
 * 
 * @author Hugo
 */
public class AudioManager {
    private Clip bgmClip;
    private Clip sfxClip;

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
            System.err.println("Erreur bgm : " + resourcePath);
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


    /**
     * Joue un sfx 
     * @param resourcePath 
     */
    public void playSfx(String resourcePath) {
        try {
            URL url = AudioManager.class.getResource(resourcePath);
            if (url == null) throw new RuntimeException("Fichier introuvable");
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                sfxClip = AudioSystem.getClip();
                sfxClip.open(ais);
            }

            // Ferme le clip 
            sfxClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    sfxClip.close();
                }
            });

            sfxClip.start();

        } catch (Exception e) {
            System.err.println("Erreur sfx : " + resourcePath);
        }
    }
}