package fr.upc.cr1a.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleConsumer;

import javax.swing.Timer;

/**
 * Timer unique pour toutes les animations avec deltatime
 * Chaque composant qui veut être animé s'inscrit ici
 * Quand plus personne est dans la liste, le timer s'arrête tout seul
 *
 * Utilisation :
 *   AnimLoop.add(this, dt -> {
 *       valeur += vitesse * dt;      //les animations
 *       repaint();
 *   });
 *   AnimLoop.remove(this);
 *
 * Multiplie les vitesses par dt (deltatime) pour qu'elles soient pareilles sur tout les ordi
 *
 * @author Ian
 */
public class AnimLoop {

    private static final int TICK_DELAY = (int)(1000f / 60f); // 60 fps

    // delta time pour rendre pareil pour tout CPU
    private static double deltaTime = TICK_DELAY / 1000.0;
    private static long lastTime = System.nanoTime();

    // object = composant         DoubleConsumer = fonction(dt)
    private static final Map<Object, DoubleConsumer> tasks = new LinkedHashMap<>();

    private static final Timer timer = new Timer(TICK_DELAY, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            tick();
        }
    });

    private AnimLoop() {}

    /** inscrit un composant dans la boucle, demarre le timer si besoin */
    public static void add(Object key, DoubleConsumer task) {
        tasks.put(key, task);
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    /** desinscrit un composant, arrete le timer si plus personne inscrit */
    public static void remove(Object key) {
        tasks.remove(key);
        if (tasks.isEmpty()) {
            timer.stop();
        }
    }

    /**
     * Appelé à chaque tick du timer
     */
    private static void tick() {
        long now = System.nanoTime();
        deltaTime = (now - lastTime) / 1000000000.0; //combien de temps depuis le dernier tick convertis en secondes
        lastTime = now;
        double dt = deltaTime * 60; // si 1.0 = bien bien sinon ordi nul nul et tourne avec moins fps mais sans ralentir (dt = 2.0 -> 30 fps)

        for (DoubleConsumer task : tasks.values().toArray(new DoubleConsumer[0])) {
            task.accept(dt);
        }
    }
}
