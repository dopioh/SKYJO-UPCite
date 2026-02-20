
package fr.upc.cr1a.model;

import fr.upc.cr1a.model.interfaces.BotStrategyInterface;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe pour gérer les statistiques des victoires
 * enregistre le nombre de victoires des bot et leurs adversaires
 * 
 * @author Billy
 */
public class BotStatsManager {
    
    //Je vais garder les stats vous pouvez mettre en commentaire de votre coté
    private static final String STATS_FILE = System.getProperty("user.dir") + File.separator + "bot_stats.txt";
    
    /**
     * enregistre la victoire pour le gagnant
     * @param winner le joueur/bot qui a gagné
     * @param players la liste de tous les joueurs 
     */
    public static void recordVictory(PlayerModel winner, ArrayList<PlayerModel> players) {
        // Déterminer le type de gagnant
        String winnerType = getPlayerType(winner);
        
        //lire les stats déja dans le fichier
        Map<String, Integer> stats = loadStats();
        
        //incrémenter le compteur de victoires
        stats.put(winnerType, stats.getOrDefault(winnerType, 0) + 1);
        
        //enregistrer contre qui le gagnant a gagné
        for (PlayerModel player : players) {
            if (player != winner) {
                String opponentType = getPlayerType(player);
                String matchup = winnerType + " vs " + opponentType;
                stats.put(matchup, stats.getOrDefault(matchup, 0) + 1);
            }
        }
        
        //sauvegarder les stats
        saveStats(stats);
        
        //afficher les statistiques
        showStats(stats);
    }
    
    /**
     * détermine le type d'un joueur/bot
     * @param player le joueur à identifier
     * @return le type du joueur
     */
    private static String getPlayerType(PlayerModel player) {
        if (!(player instanceof BotModel)) {
            return "Joueur";
        }
        
        BotModel bot = (BotModel) player;
        BotStrategyInterface strategy = bot.getStrategy();
        
        if (strategy instanceof BotRow) {
            return "BotRow";
        } else if (strategy instanceof BotHigh) {
            int seuil = strategy.getSeuil();
            if (seuil <= 2) {
                return "BotLow";
            } else {
                return "BotHigh";
            }
        } else if (strategy instanceof BotTerminalModel) {
            return "Bot";
        } else {
            return "BotAutre";
        }
    }
    
    /**
     * charge les statistiques depuis le fichier
     * @return une map avec les statistiques
     */
    private static Map<String, Integer> loadStats() {
        Map<String, Integer> stats = new HashMap<>();
        File file = new File(STATS_FILE);
        
        if (!file.exists()) {
            return stats;
        }
        
        //lire les stats depuis le fichier
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String type = parts[0].trim();
                    int count = Integer.parseInt(parts[1].trim());
                    stats.put(type, count);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erreur lors de la lecture des statistiques: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * sauvegarde les statistiques dans le fichier
     * @param stats les statistiques à sauvegarder
     */
    private static void saveStats(Map<String, Integer> stats) {
        //écrire les stats dans le fichier
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STATS_FILE))) {
            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde des statistiques: " + e.getMessage());
        }
    }
    
    /**
     * affiche les statistiques actuelles
     * @param stats les statistiques a afficher
     */
    private static void showStats(Map<String, Integer> stats) {
        System.out.println("\nVictoires totales:");
        System.out.println("  Joueur:   " + stats.getOrDefault("Joueur", 0) + " victoire(s)");
        System.out.println("  Bot:      " + stats.getOrDefault("Bot", 0) + " victoire(s)");
        System.out.println("  BotRow:   " + stats.getOrDefault("BotRow", 0) + " victoire(s)");
        System.out.println("  BotLow:   " + stats.getOrDefault("BotLow", 0) + " victoire(s)");
        System.out.println("  BotHigh:  " + stats.getOrDefault("BotHigh", 0) + " victoire(s)");
        
        System.out.println("\nMatch rencontré :");
        String[] types = {"Joueur", "Bot", "BotRow", "BotLow", "BotHigh"};
        for (String winner : types) {
            boolean hasVictories = false;
            StringBuilder sb = new StringBuilder();
            sb.append("  ").append(winner).append(" a gagné contre:");
            
            for (String opponent : types) {
                if (!winner.equals(opponent)) {
                    String key = winner + " vs " + opponent;
                    int count = stats.getOrDefault(key, 0);
                    if (count > 0) {
                        hasVictories = true;
                        sb.append(" ").append(opponent).append("(").append(count).append(")");
                    }
                }
            }
            
            if (hasVictories) {
                System.out.println(sb.toString());
            }
        }
    }
}
