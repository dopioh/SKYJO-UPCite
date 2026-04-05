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
        setPreferredSize(new Dimension(800, 100));
        setMaximumSize(new Dimension(1000, 100));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 5));

        // police doodle
        Font baseFont;
        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            baseFont = stream != null
                ? Font.createFont(Font.TRUETYPE_FONT, stream)
                : new Font("Arial", Font.PLAIN, 12);
        } catch (Exception e) {
            baseFont = new Font("Arial", Font.PLAIN, 12);
        }
        Font fontName = baseFont.deriveFont(Font.BOLD,  29f);
        Font fontSub = baseFont.deriveFont(Font.PLAIN, 20f);
        Font fontCount = baseFont.deriveFont(Font.BOLD,  34f);
        Font fontPlayers = baseFont.deriveFont(Font.PLAIN, 18f);

        //panel gauche, infos du salon
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        infoPanel.setPreferredSize(new Dimension(450, 0));

        //nom du salon
        nameLabel = new JLabel(lobby.getLobbyName());
        nameLabel.setFont(fontName);
        nameLabel.setForeground(Color.WHITE);
        adjustLabelHeight(nameLabel);

        //host
        hostLabel = new JLabel("Host: " + lobby.getHostName());
        hostLabel.setFont(fontSub);
        hostLabel.setForeground(new Color(255, 255, 255, 220));
        adjustLabelHeight(hostLabel);

        //mode
        String modeText = lobby.isActionMode() ? "Mode: Actions activées" : "Mode: Classique";
        modeLabel = new JLabel(modeText);
        modeLabel.setFont(fontSub);
        modeLabel.setForeground(new Color(255, 255, 255, 220));
        adjustLabelHeight(modeLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0; //sinon espace bizzaro
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 2, 0); infoPanel.add(nameLabel, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, -7, 0); infoPanel.add(hostLabel, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 0, 0);  infoPanel.add(modeLabel, gbc);

        //panel central, nombre de joueurs
        JPanel playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setOpaque(false);

        playersLabel = new JLabel(lobby.getCurrentPlayers() + "/" + lobby.getMaxPlayers());
        playersLabel.setFont(fontCount);
        playersLabel.setForeground(getPlayerCountColor());
        playersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel playersText = new JLabel("Joueurs");
        playersText.setFont(fontPlayers);
        playersText.setForeground(new Color(255, 255, 255, 220));
        playersText.setAlignmentX(Component.CENTER_ALIGNMENT);

        playersPanel.add(playersLabel);
        playersPanel.add(playersText);

        //bouton rejoindr à droite
        if (lobby.canJoin()) {
            joinBtn = new DoodleButton("Rejoindre", ButtonColor.GREEN, 220, 70);
        } else {
            joinBtn = new DoodleButton("Complet", ButtonColor.RED, 220, 70);
            joinBtn.setEnabled(false);
        }
        joinBtn.setFontSize(32f);

        add(infoPanel, BorderLayout.WEST);
        add(playersPanel, BorderLayout.CENTER);
        add(joinBtn, BorderLayout.EAST);
    }

    // BoxLayout laisse trop d'espace verticalll
    private void adjustLabelHeight(JLabel label) {
        int h = (int)(label.getFont().getSize() * 1.2f);
        label.setMaximumSize(new Dimension(9999, h));
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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(228, 226, 157, 100));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);
        g2.dispose();
        super.paintComponent(g);
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
