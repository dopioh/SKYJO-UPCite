package fr.upc.cr1a.controller;

import fr.upc.cr1a.model.AudioManager;
//import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.player.PlayerAccount;
import fr.upc.cr1a.view.DoodleButton;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.MainMenuPanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//import javax.swing.JButton;
import javax.swing.SwingUtilities;
import fr.upc.cr1a.controller.data.SendAPI;
import fr.upc.cr1a.model.player.PlayerData;

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
    private final Scanner scanner = new Scanner(System.in);
    private final AccountController accountController;
    private boolean leaderboardLoading = false;

    public MainMenuController(Frame frame, MainMenuPanel view) {
        this.frame = frame;
        this.view = view;
        this.audio = frame.getAudioManager();
        this.accountController = frame.getAccountController();

        // listeners
        ajouterListeners(view.getShopButton(), () -> clicShop());
        ajouterListeners(view.getPlayButton(), () -> clicPlay());
        ajouterListeners(view.getSettingsButton(), () -> clicSettings());
        view.getAccountButton().addMouseListener(new MouseAdapter() { // n'est pas un DoodleButton donc on peut pas utiliser la fonction d'en dessous
            @Override
            public void mousePressed(MouseEvent e) { clicAccount(); }
        });
        view.getPodiumButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { clicPodium(); }
        });
        frame.getLeaderboard().getBackButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                leaderboardLoading = false; // arrete la boucle de retry si le player sort pendant le chargement
                frame.hideLeaderboard();
            }
        });
    }

    // ajoute hover + clic
    private void ajouterListeners(DoodleButton btn, Runnable action) {
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (view.isAccountStatsVisible()) {
                    return;
                }
                btn.onMouseEnter();
            }

            public void mouseExited(MouseEvent e) { 
                btn.onMouseExit(); 
            }
            
            public void mousePressed(MouseEvent e) {
                if (view.isAccountStatsVisible()) {
                    return;
                }
                action.run();
            }
        });
    }

    private void clicShop() {

        if (PlayerAccount.actuelAccount != null) {
            frame.getShopPanel().setMoney(PlayerAccount.actuelAccount.getMoney());
        }
        frame.showPanel("SHOP");
    }

    private void clicPlay() {
        //audio.playBgm("/music/calmbgm.wav");
        frame.showPanel("GAME_MODE");
    }

    private void clicSettings() {
        frame.showSettingsOverlay();
    }

    private void clicAccount() {
        view.openAccountStats();
    }

    private void clicPodium() {
        // Faux joueurs (pour tester)
         // List<PlayerData> players = new ArrayList<>();
        // players.add(new PlayerData("Ian"));
        // players.add(new PlayerData("Bob"));
        // players.add(new PlayerData("biubb"));
        // players.add(new PlayerData("oiiaiaoiiaia"));
        // players.add(new PlayerData("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHH"));
        // players.add(new PlayerData("Bob"));
        // players.add(new PlayerData("biubb"));
        // players.add(new PlayerData("oih"));
        // players.add(new PlayerData("<html>Hacker<br>haha</html>")); // faut interdir de se nommer comme ça
        // frame.showLeaderboard(players);

        // Vrai joueurs
        if (leaderboardLoading) return;
        leaderboardLoading = true;
        frame.showLeaderboard(new ArrayList<>());
        new Thread(() -> {
            List<PlayerData> players = new ArrayList<>();
            while (players.isEmpty() && leaderboardLoading) { // reessayer de load tout les 5 second si il arrive pas
                players = SendAPI.getAllPlayers();
                if (players.isEmpty()) {
                    try { Thread.sleep(5000); } catch (InterruptedException e) { break; }
                }
            }
            List<PlayerData> result = players;
            SwingUtilities.invokeLater(() -> {
                if (leaderboardLoading) frame.showLeaderboard(result);
                leaderboardLoading = false;
            });
        }).start();
    }
}
