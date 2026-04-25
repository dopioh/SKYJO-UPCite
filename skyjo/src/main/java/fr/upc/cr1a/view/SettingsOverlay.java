package fr.upc.cr1a.view;

import fr.upc.cr1a.utils.ImageCache;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Overlay des paramètres
 * 
 * @author Hugo
 */
public class SettingsOverlay extends JComponent {
    //Fenetre de l'overlay
    private final JPanel card;
    private Font doodleFont;

    //Bouton Fermer
    private final JLabel backButton;

    //Catégories paramètres
    private final JPanel contentPanel = new JPanel(new CardLayout());
    private JButton tabGraphique;
    private JButton tabAudio;
    private JButton tabAutres;

    private JPanel tabsBar;
    private JPanel selectedTabPanel;

    //Param Graphique
    private JPanel tabGraphiquePanel;
    private JButton handCardAnimationsButton;

    //Param Audio
    private JPanel tabAudioPanel;
    private JButton soundButton;
    private JButton volumeLeftButton;
    private JButton volumeValueButton;
    private JButton volumeRightButton;

    //Param autres
    private JPanel tabAutresPanel;
    private JButton fullscreenButton;

    public SettingsOverlay() {
        setOpaque(false);
        setVisible(false);
        setLayout(new GridBagLayout()); //pour centrer

        // police doodle
        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            doodleFont = Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (Exception e) {
            doodleFont = new Font("Arial", Font.BOLD, 12);
        }

        tabGraphique = createTabButton("Graphique");
        tabAudio = createTabButton("Audio");
        tabAutres = createTabButton("Autres");

        card = createCardPanel();
        card.setPreferredSize(new Dimension(700, 520));
        card.setBackground(new Color(80, 168, 74)); // vert
        card.setOpaque(false);

        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // barre de titre avec icone et croix pareil que leaderboard
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JPanel titleLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleLeft.setOpaque(false);
        try {
            Image settingsImg = ImageCache.get("/parametre2.png").getScaledInstance(36, 36, Image.SCALE_SMOOTH);
            JLabel icon = new JLabel(new ImageIcon(settingsImg));
            icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            titleLeft.add(icon);
        } catch (Exception ignored) {}
        JLabel titleLabel = new JLabel("Paramètres");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(doodleFont.deriveFont(Font.BOLD, 36f));
        titleLeft.add(titleLabel);
        titleBar.add(titleLeft, BorderLayout.CENTER);

        backButton = new JLabel("X", SwingConstants.CENTER);
        backButton.setFont(doodleFont.deriveFont(Font.BOLD, 40f));
        backButton.setForeground(new Color(72, 129, 64));
        backButton.setPreferredSize(new Dimension(46, 46));
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        titleBar.add(backButton, BorderLayout.EAST);

        //Catégories paramètres
        final Color tabNormal = new Color(65, 140, 58); // vert foncé
        final Color tabHover  = new Color(52, 115, 47);

        tabsBar = createTabsBar(tabNormal, tabHover);

        //Parametres graphique
        JPanel graphiquePanel = createGraphiquePanel();

        //Parametres Audio
        JPanel audioPanel = createAudioPanel();

        //parametres Autres
        JPanel autresPanel = createAutresPanel();

        contentPanel.setOpaque(false);
        contentPanel.add(graphiquePanel, "GRAPHIQUE");
        contentPanel.add(audioPanel, "AUDIO");
        contentPanel.add(autresPanel, "AUTRES");

        // titre + onglets
        JPanel northSection = new JPanel(new BorderLayout(0, 8));
        northSection.setOpaque(false);
        northSection.add(titleBar, BorderLayout.NORTH);
        northSection.add(tabsBar, BorderLayout.CENTER);

        card.add(northSection, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);

        add(card);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateCardBounds();
            }
        });
        updateCardBounds();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        
        //Fond semi-transparent
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private JPanel createCardPanel() {
        return new JPanel() {
            //Rectangle arrondi
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 50, 50));
                // Bordure
                g2.setColor(new Color(0, 0, 0, 64));
                g2.setStroke(new BasicStroke(5));
                g2.draw(new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 50, 50));
                g2.dispose();
            }
        };
    }


    private JPanel createTabsBar(Color tabNormal, Color tabHover) {
        tabGraphiquePanel = createTab(tabGraphique, tabNormal, tabHover, 18, 40);
        tabAudioPanel = createTab(tabAudio, tabNormal, tabHover, 18, 40);
        tabAutresPanel = createTab(tabAutres, tabNormal, tabHover, 18, 40);

        JPanel bar = new JPanel(new GridLayout(1, 3, 12, 0));

        bar.setOpaque(false);
        bar.add(tabGraphiquePanel);
        bar.add(tabAudioPanel);
        bar.add(tabAutresPanel);

        return bar;
    }

    // modifie ici pour la partie graphique ================================================
    private JPanel createGraphiquePanel() {
        JPanel graphiquePanel = new JPanel(new GridBagLayout());
        graphiquePanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel graphiqueLabel = new JLabel("Animations cartes", SwingConstants.CENTER);
        graphiqueLabel.setForeground(Color.WHITE);
        graphiqueLabel.setFont(doodleFont.deriveFont(Font.BOLD, 24f));

        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 12, 0);
        graphiquePanel.add(graphiqueLabel, gbc);

        final Color aNormal = new Color(65, 140, 58);
        final Color aHover  = new Color(52, 115, 47);

        JPanel handAnimationsPanel = createHoverRoundedPanel(aNormal, aHover, 18, 44);
        handCardAnimationsButton = createTextButton("HAUT", 20f);
        handAnimationsPanel.add(handCardAnimationsButton);

        handAnimationsPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { handCardAnimationsButton.doClick(); }
        });

        handCardAnimationsButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { handAnimationsPanel.setBackground(aHover);  handAnimationsPanel.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { handAnimationsPanel.setBackground(aNormal); handAnimationsPanel.repaint(); }
        });

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 60, 0, 60);
        graphiquePanel.add(handAnimationsPanel, gbc);

        return graphiquePanel;
    }

    private JPanel createAudioPanel() {
        JPanel audioPanel = new JPanel(new GridBagLayout());  
        audioPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        //On / Off
        JLabel soundLabel = new JLabel("Son", SwingConstants.CENTER);
        soundLabel.setForeground(Color.WHITE);
        soundLabel.setFont(doodleFont.deriveFont(Font.BOLD, 24f));

        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 12, 0);
        audioPanel.add(soundLabel, gbc);

        final Color sNormal = new Color(65, 140, 58);
        final Color sHover  = new Color(52, 115, 47);

        JPanel soundPanel = createHoverRoundedPanel(sNormal, sHover, 18, 44);
        soundButton = createTextButton("ON", 20f);
        soundPanel.add(soundButton);

        soundPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { soundButton.doClick(); }
        });

        soundButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { soundPanel.setBackground(sHover);  soundPanel.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { soundPanel.setBackground(sNormal); soundPanel.repaint(); }
        });

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 60, 0, 60);
        audioPanel.add(soundPanel, gbc);

        //Contrôle volume
        JLabel volLabel = new JLabel("Volume", SwingConstants.CENTER);
        volLabel.setForeground(Color.WHITE);
        volLabel.setFont(doodleFont.deriveFont(Font.BOLD, 24f));

        gbc.gridy = 2;
        gbc.insets = new Insets(28, 0, 12, 0);
        audioPanel.add(volLabel, gbc);

        JPanel volumeRow = new JPanel(new GridBagLayout());
        volumeRow.setOpaque(false);

        GridBagConstraints g2 = new GridBagConstraints();
        g2.gridy = 0;
        g2.fill = GridBagConstraints.HORIZONTAL;

        final Color aNormal = new Color(65, 140, 58);
        final Color aHover  = new Color(52, 115, 47);

        //Fleche gauche (hover)
        JPanel leftPanel = createHoverRoundedPanel(aNormal, aHover, 18, 44);
        leftPanel.setPreferredSize(new Dimension(60, 44));
        volumeLeftButton = createTextButton("<", 22f);
        leftPanel.add(volumeLeftButton);

        leftPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { volumeLeftButton.doClick(); }
        });

        volumeLeftButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { leftPanel.setBackground(aHover);  leftPanel.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { leftPanel.setBackground(aNormal); leftPanel.repaint(); }
        });

        g2.gridx = 0;
        g2.weightx = 0;
        g2.insets = new Insets(0, 0, 0, 12);
        volumeRow.add(leftPanel, g2);

        //Valeur volume (sans hover)
        JPanel valuePanel = new JPanel(new GridBagLayout());
        valuePanel.setOpaque(false);
        valuePanel.setPreferredSize(new Dimension(120, 44));

        volumeValueButton = createTextButton("10", 20f);
        valuePanel.add(volumeValueButton);

        g2.gridx = 1;
        g2.weightx = 1;
        g2.insets = new Insets(0, 0, 0, 0);
        volumeRow.add(valuePanel, g2);

        //Fleche droite (hover)
        JPanel rightPanel = createHoverRoundedPanel(aNormal, aHover, 18, 44);
        rightPanel.setPreferredSize(new Dimension(60, 44));
        volumeRightButton = createTextButton(">", 22f);
        rightPanel.add(volumeRightButton);

        rightPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { volumeRightButton.doClick(); }
        });

        volumeRightButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { rightPanel.setBackground(aHover);  rightPanel.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { rightPanel.setBackground(aNormal); rightPanel.repaint(); }
        });

        g2.gridx = 2;
        g2.weightx = 0;
        g2.insets = new Insets(0, 12, 0, 0);
        volumeRow.add(rightPanel, g2);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 60, 0, 60);
        audioPanel.add(volumeRow, gbc);

        return audioPanel;
    }

    private JPanel createAutresPanel() {
        JPanel autresPanel = new JPanel(new GridBagLayout());
        autresPanel.setOpaque(false);

        GridBagConstraints gbcA = new GridBagConstraints();
        gbcA.gridx = 0;
        gbcA.weightx = 1;
        gbcA.fill = GridBagConstraints.HORIZONTAL;

        JLabel fsLabel = new JLabel("Plein écran", SwingConstants.CENTER);
        fsLabel.setForeground(Color.WHITE);
        fsLabel.setFont(doodleFont.deriveFont(Font.BOLD, 24f));

        gbcA.gridy = 0;
        gbcA.insets = new Insets(20, 0, 12, 0);
        autresPanel.add(fsLabel, gbcA);

        final Color fsNormal = new Color(65, 140, 58);
        final Color fsHover  = new Color(52, 115, 47);

        JPanel fullScreenPanel = createHoverRoundedPanel(fsNormal, fsHover, 18, 44);
        fullscreenButton = createTextButton("OFF", 20f);
        fullScreenPanel.add(fullscreenButton);

        fullScreenPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { fullscreenButton.doClick(); }
        });

        fullscreenButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { fullScreenPanel.setBackground(fsHover);  fullScreenPanel.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { fullScreenPanel.setBackground(fsNormal); fullScreenPanel.repaint(); }
        });

        gbcA.gridy = 1;
        gbcA.insets = new Insets(0, 60, 0, 60);
        autresPanel.add(fullScreenPanel, gbcA);

        return autresPanel;
    }

    // JButton avec texte
    private JButton createTextButton(String text, float fontSize) {
        JButton b = new JButton(text);
        //Attribut basique du Jbutton supprmé
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(Color.WHITE);
        b.setFont(doodleFont.deriveFont(Font.BOLD, fontSize));
        return b;
    }

    // Panneau rond avec hover
    private JPanel createHoverRoundedPanel(Color normal, Color hover, int arc, int height) {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
                g2.dispose();
            }
        };

        p.setOpaque(false);
        p.setBackground(normal);
        p.setPreferredSize(new Dimension(0, height));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { p.setBackground(hover);  p.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { p.setBackground(normal); p.repaint(); }
        });
        return p;
    }

    //Comme le texte button
    private JButton createTabButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(Color.WHITE);
        b.setFont(doodleFont.deriveFont(Font.BOLD, 20f));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    //Fait des "tabs" (zone extendue des jbutton) cliquable
    private JPanel createTab(JButton tabButton, Color normal, Color hover, int arc, int height) {
        JPanel tabPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
                g2.dispose();
            }
        };
        tabPanel.setOpaque(false);
        tabPanel.setBackground(normal);
        tabPanel.setPreferredSize(new Dimension(0, height));
        tabPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tabPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (tabPanel != selectedTabPanel) { tabPanel.setBackground(hover); tabPanel.repaint(); }
            }
            @Override public void mouseExited(MouseEvent e) {
                tabPanel.setBackground(tabPanel == selectedTabPanel ? new Color(219, 240, 217) : normal);
                tabPanel.repaint();
            }
        });
        tabPanel.add(tabButton);
        // Hover quand la souris est sur le texte
        tabButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (tabPanel != selectedTabPanel) { tabPanel.setBackground(hover); tabPanel.repaint(); }
            }
            @Override public void mouseExited(MouseEvent e) {
                tabPanel.setBackground(tabPanel == selectedTabPanel ? new Color(219, 240, 217) : normal);
                tabPanel.repaint();
            }
        });
        return tabPanel;
    }

    private void updateCardBounds() {
        int w = getWidth();
        int h = getHeight();
        int cardW = (int) (w * 0.55);
        int cardH = (int) (h * 0.70);
        cardW = Math.max(700, Math.min(cardW, 1100));
        cardH = Math.max(520, Math.min(cardH, 800));
        card.setPreferredSize(new Dimension(cardW, cardH));
        card.revalidate();
        card.repaint();
    }

    //Pour les controllers
    public JLabel getBackButton(){return backButton;}
    public JPanel getContentPanel(){return contentPanel;}
    public CardLayout getCardLayout(){return (CardLayout) contentPanel.getLayout();}
    public JButton getTabGraphique(){ return tabGraphique;}
    public JButton getTabAudio(){ return tabAudio;}
    public JButton getTabAutres(){ return tabAutres;}
    public JPanel getTabGraphiquePanel() { return tabGraphiquePanel;}
    public JPanel getTabAudioPanel() { return tabAudioPanel;}
    public JPanel getTabAutresPanel() { return tabAutresPanel;}
    public JButton getFullscreenButton() { return fullscreenButton;}
    public JButton getSoundButton() { return soundButton;}
    public JButton getVolumeLeftButton() { return volumeLeftButton;}
    public JButton getVolumeValueButton() { return volumeValueButton;}
    public JButton getVolumeRightButton() { return volumeRightButton;}
    public JButton getHandCardAnimationsButton() { return handCardAnimationsButton;}

    public void setSelectedTabPanel(JComponent tabPanel) {
        selectedTabPanel = (JPanel) tabPanel;
        Color active = new Color(219, 240, 217); // clair
        Color normal = new Color(65, 140, 58);   // vert foncé
        tabGraphiquePanel.setBackground(tabPanel == tabGraphiquePanel ? active : normal);
        tabAudioPanel.setBackground(tabPanel == tabAudioPanel ? active : normal);
        tabAutresPanel.setBackground(tabPanel == tabAutresPanel ? active : normal);
        tabGraphique.setForeground(tabPanel == tabGraphiquePanel ? normal : Color.WHITE);
        tabAudio.setForeground(tabPanel == tabAudioPanel ? normal : Color.WHITE);
        tabAutres.setForeground(tabPanel == tabAutresPanel ? normal : Color.WHITE);
        if (tabsBar != null) tabsBar.repaint();
    }
}
