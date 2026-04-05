package fr.upc.cr1a.view;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.*;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.model.PlayerModel;

/**
 * Panel affiché entre les manches avec classement des joueurs et bouton continuer/menu
 * 
 * @author Ian
 */
public class RoundEndPanel extends JPanel {

    private DoodleButton btnContinuer;
    private DoodleButton btnRetourMenu;
    private PlayerPreviewPanel previewPanel;
    private JLayeredPane layeredPane;
    private boolean gameOver;
    private List<JLabel> playerLabels = new ArrayList<>();
    private List<PlayerModel> sortedPlayers;
    private PlayerModel roundEndPlayer;

    public RoundEndPanel(List<PlayerModel> players, boolean gameOver, PlayerModel roundEndPlayer) {
        this.gameOver = gameOver;
        this.roundEndPlayer = roundEndPlayer;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(true);
        contentPanel.setBackground(Color.WHITE);
        layeredPane.add(contentPanel, JLayeredPane.DEFAULT_LAYER);

        // Preview panel hover
        previewPanel = new PlayerPreviewPanel();
        previewPanel.setVisible(false);
        layeredPane.add(previewPanel, JLayeredPane.PALETTE_LAYER);

        // fix écran blanc au premier affichage (JLayeredPane sans layout)
        final JPanel contentRef = contentPanel;
        SwingUtilities.invokeLater(() -> {
            Dimension size = layeredPane.getSize();
            if (size.width > 0 && size.height > 0) {
                contentRef.setBounds(0, 0, size.width, size.height);
                previewPanel.setBounds(size.width / 4, size.height / 6, size.width / 2, size.height * 2 / 3);
            }
            layeredPane.revalidate();
            layeredPane.repaint();
        });

        //titre avec information du déclencheur
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        String title = gameOver ? "Partie terminée" : "Fin de la manche";
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(titleLabel);

        //ajouter le nom du joueur qui a déclenché la fin
        if (!gameOver && roundEndPlayer != null) {
            JLabel roundEndInfo = new JLabel("Déclenchée par : " + roundEndPlayer.getName(), SwingConstants.CENTER);
            roundEndInfo.setFont(new Font("Arial", Font.ITALIC, 16));
            roundEndInfo.setForeground(new Color(100, 100, 100));
            roundEndInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
            titlePanel.add(Box.createVerticalStrut(5));
            titlePanel.add(roundEndInfo);
        }

        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // gauche : liste des joueurs trié par score
        sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort(Comparator.comparingInt(p -> p.getTotalScore()));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 20));

        for (int i = 0; i < sortedPlayers.size(); i++) {
            PlayerModel p = sortedPlayers.get(i);

            //construction du texte avec détails
            StringBuilder text = new StringBuilder();
            text.append(i + 1).append(" - ").append(p.getName()).append("  ");

            // Afficher les détails de la manche si disponibles
            if (!gameOver && p.getLastRoundScore() != null) {
                PlayerModel.RoundScore roundScore = p.getLastRoundScore();
                text.append("(Main: ").append(roundScore.handScore).append("p");

                if (roundScore.actionPenalty > 0) {
                    text.append(" + Actions: ").append(roundScore.actionPenalty).append("p");
                }

                //afficher le doublement si applicable
                if (roundScore.wasDoubled) {
                    text.append(" × 2 = +").append(roundScore.totalAdded).append("p) ");
                } else {
                    text.append(" = +").append(roundScore.totalAdded).append("p) ");
                }
            }

            text.append("Total: ").append(p.getTotalScore()).append("p");

            JLabel label = new JLabel(text.toString());
            label.setFont(new Font("Arial", Font.PLAIN, 18));
            label.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            playerLabels.add(label);
            listPanel.add(label);
        }

        contentPanel.add(listPanel, BorderLayout.CENTER);

        // Droite : zone bouton
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 40));

        GridBagConstraints gbcBtn = new GridBagConstraints();
        gbcBtn.gridx = 0;
        gbcBtn.gridy = 0;
        gbcBtn.insets = new Insets(10, 10, 10, 10);

        if (!gameOver) {
            btnContinuer = new DoodleButton("Continuer", ButtonColor.GREEN, 320, 80, "/Button/Continue.png");
            rightPanel.add(btnContinuer, gbcBtn);
        } else {
            PlayerModel winner = sortedPlayers.get(0);
            JLabel winLabel = new JLabel("Gagnant : " + winner.getName(), SwingConstants.CENTER);
            winLabel.setFont(new Font("Arial", Font.BOLD, 22));
            rightPanel.add(winLabel, gbcBtn);

            gbcBtn.gridy = 1;
            btnRetourMenu = new DoodleButton("Menu", ButtonColor.RED, 200, 70);
            rightPanel.add(btnRetourMenu, gbcBtn);
        }

        contentPanel.add(rightPanel, BorderLayout.EAST);
    }

    public void showPreview(PlayerModel player) {
        previewPanel.showOtherPlayerHand(player);
        previewPanel.setVisible(true);
    }

    public void hidePreview() {
        previewPanel.setVisible(false);
    }

    public List<JLabel> getPlayerLabels() {
        return playerLabels;
    }

    public List<PlayerModel> getSortedPlayers() {
        return sortedPlayers;
    }

    public DoodleButton getBtnContinuer() {
        return btnContinuer;
    }

    public DoodleButton getBtnRetourMenu() {
        return btnRetourMenu;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
