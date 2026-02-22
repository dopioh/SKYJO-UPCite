package fr.upc.cr1a.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache contenant les images du jeu dans un dico
 * permet de NE PAS charger plusieurs fois la même image dans le disque CAR chaque image est chargée qu'une seule fois
 * @author Nassim
 * 
 * UTILISATION dans un fichier qui a besoin d'une image:
 * BufferedImage img = ImageCache.get("/chemin/vers/image.png");
 * 
 */
public class ImageCache {
    
    private static final Map<String, BufferedImage> cache = new HashMap<>();
    
    /**
     * si l'image n'est pas chargée dans le dico on la charge et on la stocke
     * sinon on la retourne directement
     * @param path chemin de l'image dans /resources
     * @return l'image chargée ou null en cas d'erreur
     */
    public static BufferedImage get(String path) {
        if (path == null) {
            return null;
        }

        if (cache.containsKey(path)) {
            return cache.get(path);
        }
        
        try {
            BufferedImage image = ImageIO.read(ImageCache.class.getResourceAsStream(path));
            if (image != null) {
                cache.put(path, image);
                System.out.println("[ImageCache] Chargée: " + path + " (" + cache.size() + " images en cache)");
            }
            return image;
        } catch (Exception e) {
            System.err.println("[ImageCache] Erreur chargement: " + path + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * charge plusieurs images à l'avance dans le cache (pour les images du menu par ex)
     * @param paths liste des chemins d'images à précharger
     */
    public static void preload(String... paths) {
        for (String path : paths) {
            get(path);
        }
    }
    
    /**
     * libere le cache
     */
    public static void clear() {
        cache.clear();
        System.out.println("[ImageCache] Cache vidé");
    }
    
    /**
     * @return retourne le nombre d'images en cache
     */
    public static int size() {
        return cache.size();
    }
}

