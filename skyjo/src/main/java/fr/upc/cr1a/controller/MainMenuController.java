package fr.upc.cr1a.controller;

import fr.upc.cr1a.model.AudioManager;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.PlayerData;
import fr.upc.cr1a.view.DoodleButton;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.MainMenuPanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Controller du menu principal avec les bouttons existants (jouer, boutique, paramètres)
 * 
 * @author Hugo
 * @author Ian
 */
public class MainMenuController {

    private final Frame frame;
    private final MainMenuPanel view;
    private final AudioManager audio;

    public MainMenuController(Frame frame, MainMenuPanel view) {
        this.frame = frame;
        this.view = view;
        this.audio = frame.getAudioManager();

        // listeners
        ajouterListeners(view.getShopButton(), () -> clicShop());
        ajouterListeners(view.getPlayButton(), () -> clicPlay());
        ajouterListeners(view.getSettingsButton(), () -> clicSettings());
    }

    // ajoute hover + clic
    private void ajouterListeners(DoodleButton btn, Runnable action) {
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.onMouseEnter(); }
            public void mouseExited(MouseEvent e) { btn.onMouseExit(); }
            public void mouseClicked(MouseEvent e) { action.run(); }
        });
    }

    private void clicShop() {
        // ici mettreeeee
    }

    private void clicPlay() {
        //audio.playBgm("/music/calmbgm.wav");
        frame.showPanel("PLAYERS");
        //new GameManager(frame); Lance le jeu 

        //mais on a besoin d'un ecran temporaire (plus tard ca sera la selection des joueurs/pseudo ou le mode de jeu en ligne ou local)
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            List<PlayerData> playerNames = new ArrayList<>();
            int botCounter = 0;
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

            for (int i = 1; i <= nbJoueurs; i++) {
                System.out.print("Nom du joueur " + i + " ou 'bot' : ");
                String inputName = scanner.nextLine();

                fr.upc.cr1a.model.PlayerData tmp;
                if (inputName.equalsIgnoreCase("bot")) {
                    botCounter++;
                    String botName = "Bot" + botCounter;
                    tmp = new fr.upc.cr1a.model.PlayerData(botName, true);
                } else {
                    tmp = new fr.upc.cr1a.model.PlayerData(inputName);
                }
                playerNames.add(tmp);
            }
            System.out.println("------------------------------------------------------------------");
            new GameManager(frame, playerNames);
            scanner.close();
        }).start();
    }

    private void clicSettings() {
        frame.showSettingsOverlay();
    }
}
