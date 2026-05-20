package fr.upc.cr1a.view;

import fr.upc.cr1a.model.LobbyPlayerModel;
import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;

import javax.swing.*;
import java.awt.*;

/**
 * Composant représentant un slot de joueur dans le salon d'attente
 *
 */
public class PlayerLobbySlot extends JPanel {

    private LobbyPlayerModel player;
    private JLabel nameLabel;
    private JLabel statusLabel;
    private JLabel roleLabel;
    private JButton kickButton;
    private boolean isLocalPlayerHost;

    public PlayerLobbySlot(LobbyPlayerModel player, boolean isLocalPlayerHost) {
        this.player = player;
        this.isLocalPlayerHost = isLocalPlayerHost;
        Font doodle;
        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            doodle = stream != null ? Font.createFont(Font.TRUETYPE_FONT, stream) : new Font("Arial", Font.PLAIN, 12);
        } catch (Exception e) {
            doodle = new Font("Arial", Font.PLAIN, 12);
        }

        setLayout(new BorderLayout(10, 0));
        setPreferredSize(new Dimension(400, 75));
        setMaximumSize(new Dimension(900, 75));
        setOpaque(true);

        //couleur de fond selon le statut
        updateBackgroundColor();

        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        //panel gauche infos du joueur
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        //nom du joueur avec badge host/bot
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        namePanel.setOpaque(false);

        nameLabel = new JLabel(player.getPlayerName());
        nameLabel.setFont(doodle.deriveFont(Font.BOLD, 18f));
        nameLabel.setForeground(new Color(40, 40, 40));
        namePanel.add(nameLabel);

        //badge Host
        if (player.isHost()) {
            roleLabel = new JLabel("HOST");
            roleLabel.setFont(doodle.deriveFont(Font.BOLD, 12f));
            roleLabel.setForeground(Color.WHITE);
            roleLabel.setBackground(new Color(255, 152, 0));
            roleLabel.setOpaque(true);
            roleLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            namePanel.add(roleLabel);
        }

        //badge Bot
        if (player.isBot()) {
            JLabel botLabel = new JLabel("BOT");
            botLabel.setFont(doodle.deriveFont(Font.BOLD, 12f));
            botLabel.setForeground(Color.WHITE);
            botLabel.setBackground(new Color(100, 100, 100));
            botLabel.setOpaque(true);
            botLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            namePanel.add(botLabel);
        }

        infoPanel.add(namePanel);
        infoPanel.add(Box.createVerticalStrut(3));

        //statut
        statusLabel = new JLabel(getStatusText());
        statusLabel.setFont(doodle.deriveFont(Font.BOLD, 14f));
        statusLabel.setForeground(getStatusColor());
        infoPanel.add(statusLabel);

        add(infoPanel, BorderLayout.WEST);

        //bouton kick à droite
        if (isLocalPlayerHost && !player.isHost()) {
            kickButton = new JButton();
            kickButton.setText("X");
            kickButton.setFont(new Font("SansSerif", Font.BOLD, 18));
            kickButton.setMinimumSize(new Dimension(50, 50));
            kickButton.setPreferredSize(new Dimension(50, 50));
            kickButton.setMaximumSize(new Dimension(50, 50));
            kickButton.setBackground(new Color(244, 67, 54));
            kickButton.setForeground(Color.WHITE);
            kickButton.setBorder(BorderFactory.createEmptyBorder());
            kickButton.setFocusPainted(false);
            kickButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            kickButton.setToolTipText("Retirer ce joueur");

            //effet hover
            kickButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    kickButton.setBackground(new Color(211, 47, 47));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    kickButton.setBackground(new Color(244, 67, 54));
                }
            });

            add(kickButton, BorderLayout.EAST);
        }
    }

    private void updateBackgroundColor() {
        if (player.isReady()) {
            setBackground(new Color(200, 230, 201)); //vert clair
        } else {
            setBackground(new Color(255, 245, 230)); //orange clair
        }
    }

    private String getStatusText() {
        if (player.isBot()) {
            return "Bot - Prêt";
        }
        return player.isReady() ? "Prêt" : "En attente...";
    }

    private Color getStatusColor() {
        return player.isReady() ? new Color(56, 142, 60) : new Color(255, 152, 0);
    }

    public void updatePlayerStatus() {
        statusLabel.setText(getStatusText());
        statusLabel.setForeground(getStatusColor());
        updateBackgroundColor();
        revalidate();
        repaint();
    }

    public LobbyPlayerModel getPlayer() {
        return player;
    }

    public JButton getKickButton() {
        return kickButton;
    }
}
