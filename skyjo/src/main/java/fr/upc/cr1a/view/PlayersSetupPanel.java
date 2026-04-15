package fr.upc.cr1a.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.utils.ImageCache;
import java.awt.image.BufferedImage;

public class PlayersSetupPanel extends JPanel {

    private final DoodleButton btnBack;
    private final DoodleButton btnLaunch;
    private JPanel playerCountButtonsPanel;
    private int selectedPlayerCount = 2;
    private JPanel playersContainer;
    private JToggleButton actionModeToggle;

    private JLabel cardPreview;
    private JPanel cardsPreviewPanel;
    private ImageIcon emptyCardIcon;
    private ImageIcon actionCardIcon;

    private Image backgroundImage;
    private BufferedImage cachedBackground;
    private Font doodle;

    public PlayersSetupPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        backgroundImage = ImageCache.get("/background.png");
        doodle = loadFont("/PatrickHandSC.ttf");

        // haut : retour à gauche et lancer à droite
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        btnBack = new DoodleButton("Retour", ButtonColor.RED, 220, 60, "/Button/retour.png");
        btnBack.setFontSize(32f);
        btnBack.setIconSide(DoodleButton.IconSide.LEFT);
        topBar.add(btnBack);

        JPanel topBarRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topBarRight.setOpaque(false);
        topBarRight.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        btnLaunch = new DoodleButton("Lancer", ButtonColor.GREEN, 220, 60, "/Button/Play_vert.png");
        btnLaunch.setFontSize(32f);
        topBarRight.add(btnLaunch);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(topBar, BorderLayout.WEST);
        topRow.add(topBarRight, BorderLayout.EAST);

        // Titre
        JLabel title = new JLabel("Configuration des joueurs", SwingConstants.CENTER);
        title.setFont(doodle.deriveFont(Font.BOLD, 48f));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(topRow, BorderLayout.NORTH);
        northPanel.add(title, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // Partie centrale
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        // Sélection du nombre de joueurs
        JPanel configTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        configTop.setOpaque(false);
        configTop.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JLabel playerCountLabel = new JLabel("Nombre de joueurs :");
        playerCountLabel.setFont(doodle.deriveFont(Font.BOLD, 26f));
        playerCountLabel.setForeground(Color.WHITE);
        configTop.add(playerCountLabel);

        playerCountButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        playerCountButtonsPanel.setOpaque(false);

        for (int i = 2; i <= 8; i++) {
            playerCountButtonsPanel.add(makeCountButton(i, i == 2));
        }

        configTop.add(playerCountButtonsPanel);
        center.add(configTop, BorderLayout.NORTH);

        // Container (gauche)
        playersContainer = new JPanel();
        playersContainer.setLayout(new BoxLayout(playersContainer, BoxLayout.Y_AXIS));
        playersContainer.setOpaque(false);
        playersContainer.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 20));

        JPanel playersHeader = new JPanel();
        playersHeader.setLayout(new BoxLayout(playersHeader, BoxLayout.Y_AXIS));
        playersHeader.setOpaque(false);
        playersHeader.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        JLabel playersTitle = new JLabel("Joueurs de la partie");
        playersTitle.setFont(doodle.deriveFont(Font.BOLD, 28f));
        playersTitle.setForeground(Color.WHITE);
        playersTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel playersSubtitle = new JLabel("Configure les participants avant le lancement");
        playersSubtitle.setFont(doodle.deriveFont(Font.PLAIN, 20f));
        playersSubtitle.setForeground(new Color(255, 255, 255, 180));
        playersSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel helpLabel = new JLabel("?", SwingConstants.CENTER);
        helpLabel.setFont(doodle.deriveFont(Font.BOLD, 22f));
        helpLabel.setForeground(Color.WHITE);

        JPanel helpBtn = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        helpBtn.setOpaque(false);
        helpBtn.setPreferredSize(new Dimension(36, 34));
        helpBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helpBtn.add(helpLabel, BorderLayout.CENTER);
        helpBtn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                JOptionPane.showMessageDialog(PlayersSetupPanel.this,
                        "bot               : blabla\n" +
                        "botRow         : abcddddd\n" +
                        "botLow         : bonjour\n" +
                        "botCounting : je laisse le fondateur des bots remplir",
                        "Types de bots", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        titleRow.add(playersTitle);
        titleRow.add(helpBtn);

        playersHeader.add(titleRow);
        // playersHeader.add(Box.createVerticalStrut(4));
        // playersHeader.add(playersSubtitle);

        JScrollPane scroll = new JScrollPane(playersContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        playersHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(playersHeader);
        leftPanel.add(scroll);

        // Panel options
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        JLabel optionsTitle = new JLabel("Options");
        optionsTitle.setFont(doodle.deriveFont(Font.BOLD, 28f));
        optionsTitle.setForeground(Color.WHITE);
        optionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel optionsSubtitle = new JLabel("Personnalise la partie");
        optionsSubtitle.setFont(doodle.deriveFont(Font.PLAIN, 20f));
        optionsSubtitle.setForeground(new Color(255, 255, 255, 180));
        optionsSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        actionModeToggle = new JToggleButton("Mode Skyjo Action") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? new Color(76, 175, 80) : new Color(255, 255, 255, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        actionModeToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionModeToggle.setFocusPainted(false);
        actionModeToggle.setContentAreaFilled(false);
        actionModeToggle.setBorderPainted(false);
        actionModeToggle.setOpaque(false);
        actionModeToggle.setMargin(new Insets(6, 14, 6, 14));
        actionModeToggle.setForeground(Color.WHITE);
        actionModeToggle.setFont(doodle.deriveFont(Font.BOLD, 22f));

        JLabel actionDescription = new JLabel("Ajoute des cartes spéciales et des effets supplémentaires");
        actionDescription.setFont(doodle.deriveFont(Font.PLAIN, 21f));
        actionDescription.setForeground(new Color(255, 255, 255, 180));
        actionDescription.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardsPreviewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        cardsPreviewPanel.setOpaque(false);
        cardsPreviewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        emptyCardIcon = loadResizedIconFromCache("/cartes/carteVide.png", 200, 220);
        actionCardIcon = loadResizedIconFromCache("/cartes/carteActionDos.png", 200, 220);

        cardPreview = new JLabel();
        cardPreview.setIcon(emptyCardIcon);
        cardPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsPreviewPanel.add(cardPreview);

        // Box transparent option
        JPanel optionsContent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        optionsContent.setLayout(new BoxLayout(optionsContent, BoxLayout.Y_AXIS));
        optionsContent.setOpaque(false);
        optionsContent.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        optionsContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsContent.add(actionModeToggle);
        optionsContent.add(Box.createVerticalStrut(8));
        optionsContent.add(actionDescription);
        optionsContent.add(Box.createVerticalStrut(8));
        optionsContent.add(cardsPreviewPanel);

        optionsPanel.add(optionsTitle);
        // optionsPanel.add(Box.createVerticalStrut(4));
        // optionsPanel.add(optionsSubtitle);
        optionsPanel.add(Box.createVerticalStrut(10));
        optionsPanel.add(optionsContent);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, optionsPanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerSize(0);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        center.add(splitPane, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        rebuildPlayerRows(2);
    }

    private JButton makeCountButton(int number, boolean selected) {
        JButton btn = new JButton(String.valueOf(number)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(52, 38));
        btn.setFont(doodle.deriveFont(Font.BOLD, 22f));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(selected ? new Color(76, 175, 80) : new Color(255, 255, 255, 80));
        btn.setForeground(Color.WHITE);
        btn.setActionCommand(String.valueOf(number));
        return btn;
    }

    private Font loadFont(String path) {
        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream != null) return Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (Exception ignored) {}
        return new Font("Arial", Font.PLAIN, 12);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            int w = getWidth(), h = getHeight();
            if (cachedBackground == null || cachedBackground.getWidth() != w || cachedBackground.getHeight() != h) {
                cachedBackground = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D bg = cachedBackground.createGraphics();
                bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                bg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                bg.drawImage(backgroundImage, 0, 0, w, h, null);
                bg.dispose();
            }
            g.drawImage(cachedBackground, 0, 0, null);
        }
    }

    public DoodleButton getBtnBack() { 
        return btnBack; 
    }

    public DoodleButton getBtnLaunch() { 
        return btnLaunch; 
    }
    
    public JPanel getPlayerCountButtonPanel() { 
        return playerCountButtonsPanel; 
    }

    public int getSelectedPlayerCount() { 
        return selectedPlayerCount; 
    }

    public void setSelectedPlayerCount(int count) { 
        this.selectedPlayerCount = count; 
    }

    public JPanel getPlayersContainer() { 
        return playersContainer; 
    }

    public JToggleButton getActionModeToggle() { 
        return actionModeToggle; 
    }

    public boolean isSkyjoActionEnabled() { 
        return actionModeToggle.isSelected(); 
    }

    /**
     * Sert a refaire le container de la partie centrale en fonction du nombre de joueurs
     * @param count nombre de joueurs
     */
    public void rebuildPlayerRows(int count) {
        playersContainer.removeAll();
        Color textColor = Color.WHITE;
        for (int i = 1; i <= count; i++) {
            final int playerIndex = i;
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 11)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            row.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setOpaque(false);
            row.setBackground(new Color(76, 175, 80));

            JLabel label = new JLabel("Joueur " + playerIndex + " : ");
            label.setFont(doodle.deriveFont(Font.BOLD, 22f));
            label.setForeground(textColor);

            JTextField nameField = new JTextField(12) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            nameField.setOpaque(false);
            nameField.setPreferredSize(new Dimension(120, 30));
            nameField.setFont(doodle.deriveFont(Font.PLAIN, 20f));
            nameField.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
            nameField.setText("Joueur " + playerIndex);
            nameField.setForeground(textColor);
            nameField.setCaretColor(Color.WHITE);
            nameField.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (nameField.getText().equals("Joueur " + playerIndex)) {
                        nameField.setText("");
                    } else {
                        nameField.selectAll();
                    }
                }
            });

            JCheckBox botCheck = new JCheckBox("Bot");
            botCheck.setFont(doodle.deriveFont(Font.BOLD, 20f));
            botCheck.setForeground(textColor);
            botCheck.setOpaque(false);
            botCheck.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JComboBox<String> botType = new JComboBox<>(new String[]{"bot", "botRow", "botLow", "botCounting"});
            botType.setFont(doodle.deriveFont(Font.BOLD, 16f));
            botType.setEnabled(false);

            botCheck.addActionListener(e -> {
                boolean isBot = botCheck.isSelected();
                nameField.setEnabled(!isBot);
                botType.setEnabled(isBot);
                if (isBot) {
                    nameField.setText("Bot" + playerIndex);
                    row.setBackground(new Color(218, 113, 72));
                } else {
                    nameField.setText("Joueur " + playerIndex);
                    row.setBackground(new Color(76, 175, 80));
                }
            });
            row.add(label);
            row.add(nameField);
            row.add(botCheck);
            row.add(botType);
            playersContainer.add(row);
            playersContainer.add(Box.createVerticalStrut(8));
        }
        playersContainer.revalidate();
        playersContainer.repaint();
    }

    private ImageIcon loadResizedIconFromCache(String path, int width, int height) {
        BufferedImage bufferedImage = ImageCache.get(path);

        if (bufferedImage == null) {
            System.err.println("Image introuvable dans le cache : " + path);
            return null;
        }

        return new ImageIcon(bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    public void updateCardPreview(boolean actionEnabled) {
        if (actionEnabled) {
            cardPreview.setIcon(actionCardIcon);
        } else {
            cardPreview.setIcon(emptyCardIcon);
        }

        cardsPreviewPanel.revalidate();
        cardsPreviewPanel.repaint();
    }
}