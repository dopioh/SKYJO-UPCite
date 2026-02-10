package fr.upc.cr1a.model;

import fr.upc.cr1a.view.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe pour lancer le jeu 
 * Elle doit servir que pour ca et pas pour autre chose
 */
public class GameLauncher {

    private GameManager gameManager;
    private Scanner scanner;
    private int botCounter = 0; // compteur bot
    
    public GameLauncher() {
        // Fenetre du jeu
        //new Frame();

        // Jeu dams le Terminal 
        scanner = new Scanner(System.in);
        startGame();
    }
    
    /**
     * Démarre le jeu et gère le menu principal
     */
    private void startGame() {
        List<PlayerData> playerNames = new ArrayList<>();
        //demande le nombre de joueurs
        int nbJoueurs = 0;
        boolean valid = false;
        do {
            try {
                System.out.print("Nombre de joueurs (2-8) : ");
                nbJoueurs = scanner.nextInt();
                scanner.nextLine();
                
                if (nbJoueurs < 2 || nbJoueurs > 8) {
                    System.out.println("Le nombre de joueurs doit être entre 2 et 8");
                } else {
                    valid = true;
                }
            } catch (Exception e) {
                System.out.println("Il te manque une case a toi, on t'as dis un chiffre");
                scanner.nextLine();
            }
        } while (!valid);
        
        //Donnez le nom des joueurs
        for (int i = 1; i <= nbJoueurs; i++) {
            System.out.print("Nom du joueur " + i + " ou 'bot' : ");
            String inputName = scanner.nextLine();
            
            PlayerData tmp;
            if (inputName.equalsIgnoreCase("bot")) {
                botCounter++;
                String botName = "Bot" + botCounter;
                tmp = new PlayerData(botName, true);
            } else {
                tmp = new PlayerData(inputName);
            }
            playerNames.add(tmp);
        }
        System.out.println("------------------------------------------------------------------");
        
        //créer le game manager
        gameManager = new GameManager(playerNames);
        
        // Lancer la partie
        gameManager.playGame();
        
        scanner.close();
    }

    public static void main(String[] args) throws Exception {
        new GameLauncher();
    }
}
