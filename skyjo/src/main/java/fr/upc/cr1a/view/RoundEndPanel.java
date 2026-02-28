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

    public RoundEndPanel(List<PlayerModel> players, boolean gameOver) {
        this.gameOver = gameOver;
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

        // Titre
        String title = gameOver ? "Partie terminée" : "Fin de la manche";
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // gauche : liste des joueurs trié par score
        sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort(Comparator.comparingInt(p -> p.getTotalScore()));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 20));

        for (int i = 0; i < sortedPlayers.size(); i++) {
            PlayerModel p = sortedPlayers.get(i);
            String text = (i + 1) + " - " + p.getName() + "  " + p.getTotalScore() + "p";
            JLabel label = new JLabel(text);
            label.setFont(new Font("Arial", Font.PLAIN, 20));
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
