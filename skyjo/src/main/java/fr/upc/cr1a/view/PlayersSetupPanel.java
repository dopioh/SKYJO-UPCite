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

    public PlayersSetupPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        //Partie du hauit
        JLabel title = new JLabel("Configuration des joueurs", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        center.setOpaque(false);

        JPanel configTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        configTop.setOpaque(false);
        configTop.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JLabel playerCountLabel = new JLabel("Nombre de joueurs (2 à 8) :");
        playerCountLabel.setFont(new Font("Arial", Font.BOLD, 15));

        configTop.add(playerCountLabel);

        playerCountButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        playerCountButtonsPanel.setOpaque(false);

        for (int i = 2; i <= 8; i++) {
            JButton btn = new JButton(String.valueOf(i));
            btn.setPreferredSize(new Dimension(42, 32));
            btn.setFont(new Font("Arial", Font.BOLD, 14));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBackground(i == 2 ? new Color(200, 220, 255) : new Color(235, 235, 235));
            btn.setActionCommand(String.valueOf(i));
            playerCountButtonsPanel.add(btn);
        }

        configTop.add(playerCountButtonsPanel);
        center.add(configTop, BorderLayout.NORTH);

        // Partie centrale
        playersContainer = new JPanel();

        // Container (gauche) 
        playersContainer = new JPanel();
        playersContainer.setLayout(new BoxLayout(playersContainer, BoxLayout.Y_AXIS));
        playersContainer.setOpaque(false);

        JPanel playersHeader = new JPanel();
        playersHeader.setLayout(new BoxLayout(playersHeader, BoxLayout.Y_AXIS));
        playersHeader.setOpaque(true);
        playersHeader.setBackground(new Color(238, 242, 250));  // COULEUR BORDURE Haut + Gauche
        playersHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel playersTitle = new JLabel("Joueurs de la partie");
        playersTitle.setFont(new Font("Arial", Font.BOLD, 18));
        playersTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel playersSubtitle = new JLabel("Configure les participants avant le lancement");
        playersSubtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        playersSubtitle.setForeground(new Color(100, 100, 100));
        playersSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        playersHeader.add(playersTitle);
        playersHeader.add(Box.createVerticalStrut(4));
        playersHeader.add(playersSubtitle);

        JScrollPane scroll = new JScrollPane(playersContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200,200,200);
                this.trackColor = new Color(245,245,245);
            }
        });

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        playersHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(playersHeader);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(scroll);
        
        // Panel a droite du container
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(true);
        optionsPanel.setBackground(new Color(245, 245, 245));
        optionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,200,200)),
                BorderFactory.createEmptyBorder(15,15,15,15)
        ));

        JLabel optionsTitle = new JLabel("Options");
        optionsTitle.setFont(new Font("Arial", Font.BOLD, 18));
        optionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel optionsSubtitle = new JLabel("Personnalise la partie");
        optionsSubtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        optionsSubtitle.setForeground(new Color(100, 100, 100));
        optionsSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        actionModeToggle = new JToggleButton("Mode Skyjo Action");
        actionModeToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionModeToggle.setFocusPainted(false);
        actionModeToggle.setSelected(false);

        JLabel actionDescription = new JLabel("Ajoute des cartes spéciales et des effets supplémentaires");
        actionDescription.setFont(new Font("Arial", Font.PLAIN, 12));
        actionDescription.setForeground(new Color(90, 90, 90));
        actionDescription.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardsPreviewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        cardsPreviewPanel.setOpaque(false);
        cardsPreviewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        emptyCardIcon = loadResizedIconFromCache("/cartes/carteVide.png", 200, 220);
        actionCardIcon = loadResizedIconFromCache("/cartes/carteActionDos.png", 200, 220);

        cardPreview = new JLabel();
        cardPreview.setIcon(emptyCardIcon);
        cardPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPreview.setBorder(null);

        cardsPreviewPanel.add(cardPreview);

        optionsPanel.add(optionsTitle);
        optionsPanel.add(Box.createVerticalStrut(4));
        optionsPanel.add(optionsSubtitle);
        optionsPanel.add(Box.createVerticalStrut(18));
        optionsPanel.add(actionModeToggle);
        optionsPanel.add(Box.createVerticalStrut(8));
        optionsPanel.add(actionDescription);
        optionsPanel.add(Box.createVerticalStrut(8));
        optionsPanel.add(cardsPreviewPanel);
        optionsPanel.add(Box.createVerticalGlue());

        // Trait de separtation
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, optionsPanel);

        splitPane.setResizeWeight(0.7);     
        splitPane.setDividerLocation(0.5);  
        splitPane.setDividerSize(2);
        splitPane.setBorder(null);

        center.add(splitPane, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // Partie du bas
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottom.setOpaque(false);

        btnBack = new DoodleButton("Retour", ButtonColor.RED, 220, 80, null); 
        btnBack.setBgImage("/Button/button_red.png");
        btnLaunch = new DoodleButton("Lancer",ButtonColor.GREEN,220,80,"/Button/Play.png"); //Faudrait mettre un triangle du style 
        btnLaunch.setBgImage("/Button/button_green.png");

        bottom.add(btnBack);
        bottom.add(btnLaunch);

        add(bottom, BorderLayout.SOUTH);

        rebuildPlayerRows(2);
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

    public void setSelectedPlayerCount(int selectedPlayerCount) {
        this.selectedPlayerCount = selectedPlayerCount;
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
        for (int i = 1; i <= count; i++) {
            final int playerIndex = i;
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 11));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200,200,200)),
                    BorderFactory.createEmptyBorder(8,8,8,8)
            ));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            row.setOpaque(true);
            row.setBackground(new Color(245, 245, 245));

            JLabel label = new JLabel("Joueur " + playerIndex + " : ");
            
            JTextField nameField = new JTextField(12);
            nameField.setPreferredSize(new Dimension(120, 30));
            nameField.setFont(new Font("Arial", Font.PLAIN, 14));
            nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200,200,200)),
                    BorderFactory.createEmptyBorder(4,8,4,8)
            ));
            nameField.setText("Joueur " + playerIndex);
            nameField.setForeground(Color.BLACK);
            nameField.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (nameField.getText().equals("Joueur " + playerIndex)) {
                        nameField.setText("");
                    } else {
                        nameField.selectAll();
                    }
                }
            });

            JCheckBox botCheck = new JCheckBox("Bot");
            JComboBox<String> botType = new JComboBox<>(new String[]{"bot", "botRow", "botLow", "botCounting"});
            botType.setEnabled(false);

            botCheck.addActionListener(e -> {
                boolean isBot = botCheck.isSelected();
                nameField.setEnabled(!isBot);
                botType.setEnabled(isBot);
                if (isBot) {
                    nameField.setText("Bot" + playerIndex);
                    row.setBackground(new Color(225, 235, 245));
                } else {
                    nameField.setText("Joueur " + playerIndex);
                    row.setBackground(new Color(245, 245, 245));
                }
            });
            row.add(label);
            row.add(nameField);
            row.add(botCheck);
            row.add(botType);
            playersContainer.add(row);
            playersContainer.add(Box.createVerticalStrut(2)); // Mini espacement
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

        Image resized = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resized);
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