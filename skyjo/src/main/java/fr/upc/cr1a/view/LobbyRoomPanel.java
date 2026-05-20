package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.model.LobbyPlayerModel;
import fr.upc.cr1a.utils.ImageCache;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de la salle d'attente d'un salon multijoueur
 * L'hôte peut configurer les options et gérer les joueurs
 *
 */
public class LobbyRoomPanel extends JPanel {

    private JLabel lobbyTitleLabel;
    private JLabel lobbyInfoLabel;
    private JLabel playersTitleLabel;
    private DoodleButton backBtn;
    private DoodleButton readyBtn;
    private DoodleButton startBtn;
    private JPanel playersListPanel;
    private JPanel leftPanel;
    private JScrollPane playersScrollPane;
    private Image backgroundImage;
    private List<PlayerLobbySlot> playerSlots;

    //panel du parametre du salon
    private JPanel configPanel;
    private JSpinner maxPlayersSpinner;
    private JCheckBox actionModeCheckbox;
    private JButton addBotButton;

    private boolean isHost;
    private Font doodle;

    public LobbyRoomPanel() {
        setLayout(new BorderLayout());
        playerSlots = new ArrayList<>();
        this.isHost = false;
        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            doodle = stream != null ? Font.createFont(Font.TRUETYPE_FONT, stream) : new Font("Arial", Font.PLAIN, 12);
        } catch (Exception e) {
            doodle = new Font("Arial", Font.PLAIN, 12);
        }

        //fond
        backgroundImage = ImageCache.get("/background.png");

        //barre supérieure
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        //zone centrale
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        //barre inférieure avec boutons
        JPanel bottomBar = createBottomBar();
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        //bouton retour à gauche
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        backBtn = new DoodleButton("Quitter", ButtonColor.RED, 180, 50);
        leftPanel.add(backBtn);

        //titre et infos au centre
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        lobbyTitleLabel = new JLabel("Salon : Partie de SKYJO");
        lobbyTitleLabel.setFont(doodle.deriveFont(Font.BOLD, 32f));
        lobbyTitleLabel.setForeground(Color.WHITE);
        lobbyTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        lobbyInfoLabel = new JLabel("En attente de joueurs...");
        lobbyInfoLabel.setFont(doodle.deriveFont(Font.BOLD, 16f));
        lobbyInfoLabel.setForeground(new Color(230, 230, 230));
        lobbyInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(lobbyTitleLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(lobbyInfoLabel);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(centerPanel, BorderLayout.CENTER);

        return topBar;
    }

    private JPanel createCenterPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 20, 10, 20);

        //panel gauche liste des joueurs
        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(true);
        leftPanel.setBackground(new Color(245, 245, 245, 230));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        leftPanel.setPreferredSize(new Dimension(500, 400));

        playersTitleLabel = new JLabel("Joueurs (" + playerSlots.size() + "/8)");
        playersTitleLabel.setFont(doodle.deriveFont(Font.BOLD, 20f));
        playersTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        playersListPanel = new JPanel();
        playersListPanel.setLayout(new BoxLayout(playersListPanel, BoxLayout.Y_AXIS));
        playersListPanel.setOpaque(false);

        playersScrollPane = new JScrollPane(playersListPanel);
        playersScrollPane.setOpaque(false);
        playersScrollPane.getViewport().setOpaque(false);
        playersScrollPane.setBorder(null);
        playersScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        leftPanel.add(playersTitleLabel, BorderLayout.NORTH);
        leftPanel.add(playersScrollPane, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        mainPanel.add(leftPanel, gbc);

        //panel droit paramètre du salon
        configPanel = createConfigPanel();
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        mainPanel.add(configPanel, gbc);

        return mainPanel;
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true);
        panel.setBackground(new Color(255, 248, 220, 230));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 193, 7), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(350, 400));

        //titre
        JLabel configTitle = new JLabel("Paramètres du salon");
        configTitle.setFont(doodle.deriveFont(Font.BOLD, 22f));
        configTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(configTitle);
        panel.add(Box.createVerticalStrut(15));

        //nombre de joueurs max
        JPanel maxPlayersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        maxPlayersPanel.setOpaque(false);
        maxPlayersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel maxPlayersLabel = new JLabel("Joueurs max : ");
        maxPlayersLabel.setFont(doodle.deriveFont(Font.BOLD, 16f));

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(4, 2, 8, 1);
        maxPlayersSpinner = new JSpinner(spinnerModel);
        maxPlayersSpinner.setFont(doodle.deriveFont(Font.BOLD, 16f));
        maxPlayersSpinner.setPreferredSize(new Dimension(60, 30));

        maxPlayersPanel.add(maxPlayersLabel);
        maxPlayersPanel.add(maxPlayersSpinner);

        panel.add(maxPlayersPanel);
        panel.add(Box.createVerticalStrut(15));

        //mode actions
        actionModeCheckbox = new JCheckBox("Mode Action");
        actionModeCheckbox.setSelected(false);
        actionModeCheckbox.setVisible(false);
        panel.add(Box.createVerticalStrut(10));

        //section Bots
        JLabel botsLabel = new JLabel("Gérer les bots :");
        botsLabel.setFont(doodle.deriveFont(Font.BOLD, 16f));
        botsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(botsLabel);
        panel.add(Box.createVerticalStrut(10));

        addBotButton = new JButton("+ Ajouter un Bot");
        addBotButton.setFont(doodle.deriveFont(Font.BOLD, 16f));
        addBotButton.setBackground(new Color(241, 192, 31));
        addBotButton.setForeground(Color.WHITE);
        addBotButton.setOpaque(true);
        addBotButton.setBorderPainted(false);
        addBotButton.setContentAreaFilled(true);
        addBotButton.setFocusPainted(false);
        addBotButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBotButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBotButton.setMaximumSize(new Dimension(200, 40));
        panel.add(addBotButton);

        panel.add(Box.createVerticalGlue());

        //note pour l'hôte
        JTextArea noteLabel = new JTextArea(
            "En tant qu'hôte, vous pouvez :\n" +
            "• Modifier le nombre max de joueurs\n" +
            "• Ajouter des bots\n" +
            "• Retirer des joueurs (bouton ✕)"
        );
        noteLabel.setFont(doodle.deriveFont(Font.BOLD, 13f));
        noteLabel.setForeground(new Color(100, 100, 100));
        noteLabel.setOpaque(false);
        noteLabel.setEditable(false);
        noteLabel.setLineWrap(true);
        noteLabel.setWrapStyleWord(true);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(noteLabel);

        return panel;
    }

    private JPanel createBottomBar() {
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        bottomBar.setOpaque(false);

        //note pour l'hôte
        JLabel hostNote = new JLabel("En tant qu'hôte, attendez que tous soient prêts puis lancez la partie");
        hostNote.setFont(doodle.deriveFont(Font.BOLD, 20f));
        hostNote.setForeground(Color.WHITE);
        hostNote.setVisible(false); // Sera visible seulement pour l'hôte

        readyBtn = new DoodleButton("Prêt", ButtonColor.GREEN, 180, 60);
        startBtn = new DoodleButton("Lancer la partie", ButtonColor.YELLOW, 250, 60);
        startBtn.setFontSize(28f);
        startBtn.setVisible(false); // Visible seulement pour l'hôte

        bottomBar.add(hostNote);
        bottomBar.add(readyBtn);
        bottomBar.add(startBtn);

        return bottomBar;
    }

    /**
     * configure le panel pour l'hôte ou un joueur normal
     */
    public void setIsHost(boolean isHost) {
        this.isHost = isHost;
        configPanel.setVisible(isHost);
        readyBtn.setVisible(!isHost);
        startBtn.setVisible(isHost);

        //afficher la note pour l'hôte
        JPanel bottomBar = (JPanel) startBtn.getParent();
        if (bottomBar != null && bottomBar.getComponentCount() > 0) {
            Component hostNote = bottomBar.getComponent(0);
            if (hostNote instanceof JLabel) {
                hostNote.setVisible(isHost);
            }
        }

        revalidate();
        repaint();
    }

    /**
     *maj le titre du salon
     */
    public void setLobbyTitle(String title) {
        lobbyTitleLabel.setText("Salon : " + title);
    }

    /**
     *maj les infos du salon
     */
    public void setLobbyInfo(String info) {
        lobbyInfoLabel.setText(info);
    }

    /**
     *ajoute un joueur à la liste
     */
    public PlayerLobbySlot addPlayer(LobbyPlayerModel player) {
        PlayerLobbySlot slot = new PlayerLobbySlot(player, isHost);
        playerSlots.add(slot);
        playersListPanel.add(slot);
        playersListPanel.add(Box.createVerticalStrut(8));
        updatePlayersCount();
        revalidate();
        repaint();
        return slot;
    }

    /**
     *retire un joueur de la liste
     */
    public void removePlayer(String playerId) {
        playerSlots.removeIf(slot -> {
            if (slot.getPlayer().getPlayerId().equals(playerId)) {
                playersListPanel.remove(slot);
                return true;
            }
            return false;
        });
        updatePlayersCount();
        revalidate();
        repaint();
    }

    /**
     *vide la liste des joueurs
     */
    public void clearPlayers() {
        playerSlots.clear();
        playersListPanel.removeAll();
        updatePlayersCount();
        revalidate();
        repaint();
    }

    /**
     *maj le compteur de joueurs
     */
    public void updatePlayersCount() {
        int maxPlayers = (Integer) maxPlayersSpinner.getValue();
        playersTitleLabel.setText("Joueurs (" + playerSlots.size() + "/" + maxPlayers + ")");
        leftPanel.revalidate();
        leftPanel.repaint();
    }

    /**
     *refresh l'affichage de tous les slots de joueurs
     */
    public void refreshPlayerSlots() {
        for (PlayerLobbySlot slot : playerSlots) {
            slot.updatePlayerStatus();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    //getters
    public DoodleButton getBackButton() {
        return backBtn;
    }

    public DoodleButton getReadyButton() {
        return readyBtn;
    }

    public DoodleButton getStartButton() {
        return startBtn;
    }

    public JSpinner getMaxPlayersSpinner() {
        return maxPlayersSpinner;
    }

    public JCheckBox getActionModeCheckbox() {
        return actionModeCheckbox;
    }

    public JButton getAddBotButton() {
        return addBotButton;
    }

    public List<PlayerLobbySlot> getPlayerSlots() {
        return playerSlots;
    }

    public boolean isHost() {
        return isHost;
    }
}
