package fr.upc.cr1a.model;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.BooleanControl;
import java.net.URL;

/**
 * Classe gérant l'audio du jeu, comme la musique de fond (bgm) et les effets sonores (sfx)
 * playBgm("resources") : joue une musique de fond en boucle
 * playSfx("resources") : joue un effet sonore une seule fois
 * 
 * Possibilité de gérer l'audio avec setEnabled(boolean active) et setVolume(int volume)
 * 
 * @author Hugo
 */
public class AudioManager {
    private Clip bgmClip;
    private boolean enabled = true; 
    private int volume = 50;

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
                applyToClip(bgmClip);
            }
            if (enabled) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            }

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
        if (!enabled) return;
        Clip sfxClip;
        try {
            URL url = AudioManager.class.getResource(resourcePath);
            if (url == null) throw new RuntimeException("Fichier introuvable");
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                sfxClip = AudioSystem.getClip();
                sfxClip.open(ais);
                applyToClip(sfxClip);
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

    private void applyToClip(Clip clip) {
        if (clip == null) return;
        if (clip.isControlSupported(BooleanControl.Type.MUTE)) {
            var mute = (BooleanControl)
                    clip.getControl(BooleanControl.Type.MUTE);
            mute.setValue(!enabled);
        }
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            var gain = (FloatControl)
                    clip.getControl(FloatControl.Type.MASTER_GAIN);

            float db = enabled ? percentToDb(volume) : gain.getMinimum();
            db = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db));
            gain.setValue(db);
        }
    }

    //Converti une valeure pourcent en decibel, utilisable par le FloatControl
    private float percentToDb(int percent) {
        if (percent <= 0) return -80f; 
        float v = percent / 100f;
        return (float) (20.0 * Math.log10(v));
    }

    public boolean isEnabled() { return enabled; }
    public int getVolume() { return volume; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        applyToClip(bgmClip);
        if (!enabled && bgmClip != null) bgmClip.stop();
        if (enabled && bgmClip != null && !bgmClip.isRunning()) bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        applyToClip(bgmClip);
    }
}