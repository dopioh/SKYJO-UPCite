package fr.upc.cr1a.view;

import fr.upc.cr1a.model.LobbyModel;
import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;

import javax.swing.*;
import java.awt.*;

/**
 * Composant représentant un salon dans la liste
 *
 */
public class LobbyItemPanel extends JPanel {

    private final LobbyModel lobby;
    private final DoodleButton joinBtn;
    private JLabel nameLabel;
    private JLabel hostLabel;
    private JLabel playersLabel;
    private JLabel modeLabel;

    public LobbyItemPanel(LobbyModel lobby) {
        this.lobby = lobby;
        setLayout(new BorderLayout(10, 5));
        setPreferredSize(new Dimension(800, 80));
        setMaximumSize(new Dimension(1000, 80));
        setOpaque(true);
        setBackground(new Color(255, 255, 255, 200));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100, 100), 2, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        //panel gauche, infos du salon
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        //nom du salon
        nameLabel = new JLabel(lobby.getLobbyName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        nameLabel.setForeground(new Color(50, 50, 50));

        //host
        hostLabel = new JLabel("Host: " + lobby.getHostName());
        hostLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        hostLabel.setForeground(new Color(80, 80, 80));

        //mode
        String modeText = lobby.isActionMode() ? "Mode: Actions activées" : "Mode: Classique";
        modeLabel = new JLabel(modeText);
        modeLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        modeLabel.setForeground(new Color(100, 100, 100));

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(hostLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(modeLabel);

        //panel central, nombre de joueurs
        JPanel playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setOpaque(false);

        playersLabel = new JLabel(lobby.getCurrentPlayers() + "/" + lobby.getMaxPlayers());
        playersLabel.setFont(new Font("Arial", Font.BOLD, 32));
        playersLabel.setForeground(getPlayerCountColor());
        playersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel playersText = new JLabel("Joueurs");
        playersText.setFont(new Font("Arial", Font.PLAIN, 12));
        playersText.setForeground(new Color(100, 100, 100));
        playersText.setAlignmentX(Component.CENTER_ALIGNMENT);

        playersPanel.add(playersLabel);
        playersPanel.add(playersText);

        //bouton rejoindr à droite
        if (lobby.canJoin()) {
            joinBtn = new DoodleButton("Rejoindre", ButtonColor.GREEN, 180, 60);
        } else {
            joinBtn = new DoodleButton("Complet", ButtonColor.RED, 180, 60);
            joinBtn.setEnabled(false);
        }

        add(infoPanel, BorderLayout.WEST);
        add(playersPanel, BorderLayout.CENTER);
        add(joinBtn, BorderLayout.EAST);
    }

    private Color getPlayerCountColor() {
        float ratio = (float) lobby.getCurrentPlayers() / lobby.getMaxPlayers();
        if (ratio < 0.5) {
            return new Color(76, 175, 80);
        } else if (ratio < 0.8) {
            return new Color(255, 152, 0);
        } else {
            return new Color(244, 67, 54);
        }
    }

    public DoodleButton getJoinButton() {
        return joinBtn;
    }

    public LobbyModel getLobby() {
        return lobby;
    }

    public void updateLobbyInfo() {
        playersLabel.setText(lobby.getCurrentPlayers() + "/" + lobby.getMaxPlayers());
        playersLabel.setForeground(getPlayerCountColor());

        if (!lobby.canJoin() && joinBtn.isEnabled()) {
            joinBtn.setText("Complet");
            joinBtn.setCouleur(ButtonColor.RED);
            joinBtn.setEnabled(false);
        }
        revalidate();
        repaint();
    }
}
