package fr.upc.cr1a.view;

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

    //Bouton Fermer
    private final JButton backButton;

    //Catégories paramètres
    private final JPanel contentPanel = new JPanel(new CardLayout());
    private final JButton tabJeu = createTabButton("Jeu");
    private final JButton tabAudio = createTabButton("Audio");
    private final JButton tabAutres = createTabButton("Autres");

    private JPanel tabsBar;
    private JComponent selectedTabPanel;

    //Param Jeu
    private JPanel tabJeuPanel;
    private JButton botDifficultyButton;

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

        card = createCardPanel();
        card.setPreferredSize(new Dimension(700, 520));
        card.setBackground(new Color(150, 150, 150));
        card.setOpaque(false);

        //Bouton Fermer le paramtètre
        final Color normal = new Color(90, 120, 140);  //(220, 170, 60) Jaune Balatro si besoin de changer
        final Color hover  = new Color(115, 145, 165); // (245, 195, 85) 

        JPanel backPanel = createBackPanel(normal, hover);
        backButton = (JButton) backPanel.getClientProperty("backButton");

        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.add(backPanel, BorderLayout.SOUTH);

        //Catégories paramètres
        final Color tabNormal = new Color(200, 60, 60);
        final Color tabHover  = new Color(225, 85, 85);

        tabsBar = createTabsBar(tabNormal, tabHover);

        //Parametres jeu
        JPanel jeuPanel = createJeuPanel();

        //Parametres Audio
        JPanel audioPanel = createAudioPanel();

        //parametres Autres
        JPanel autresPanel = createAutresPanel();

        contentPanel.setOpaque(false);
        contentPanel.add(jeuPanel, "JEU");
        contentPanel.add(audioPanel, "AUDIO");
        contentPanel.add(autresPanel, "AUTRES");

        card.add(tabsBar, BorderLayout.NORTH);
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
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
                g2.dispose();
            }

            //Bordure de celui ci
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200, 220, 220));
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 30, 30));

                g2.dispose();
            }
        };
    }

    private JPanel createBackPanel(Color normal, Color hover) {
        JPanel backPanel = createHoverRoundedPanel(normal, hover, 20, 55);
        JButton b = createTextButton("Fermer", 18f);
        backPanel.add(b);
        
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change le curseur 
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                backPanel.dispatchEvent(new MouseEvent(
                        backPanel, MouseEvent.MOUSE_ENTERED, e.getWhen(), e.getModifiersEx(),0, 0, e.getClickCount(), e.isPopupTrigger(), e.getButton()
                ));
            }
            @Override public void mouseExited(MouseEvent e) {
                backPanel.dispatchEvent(new MouseEvent(
                        backPanel, MouseEvent.MOUSE_EXITED, e.getWhen(), e.getModifiersEx(),0, 0, e.getClickCount(), e.isPopupTrigger(), e.getButton()
                ));
            }
        });
        
        //Actionne l'actionListener lié au cointroller pour la bande
        backPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                b.doClick();
                e.consume(); //Pour éviter le relachement du clic sur le panel principal
            }
        });

        backPanel.putClientProperty("backButton", b);
        return backPanel;
    }

    private JPanel createTabsBar(Color tabNormal, Color tabHover) {
        tabJeuPanel = createTab(tabJeu, tabNormal, tabHover, 18, 40);
        tabAudioPanel = createTab(tabAudio, tabNormal, tabHover, 18, 40);
        tabAutresPanel = createTab(tabAutres, tabNormal, tabHover, 18, 40);

        selectedTabPanel = tabJeuPanel;

        JPanel bar = new JPanel(new GridLayout(1, 3, 12, 0)) {
            @Override
            protected void paintChildren(Graphics g) {
                super.paintChildren(g);
                if (selectedTabPanel == null) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Rectangle r = SwingUtilities.convertRectangle(selectedTabPanel.getParent(), selectedTabPanel.getBounds(), this);

                int cx = r.x + r.width / 2; //centre x
                int top = 2; 
                int h = 10;
                int w = 14; 

                Polygon tri = new Polygon(new int[]{cx, cx - w / 2, cx + w / 2},new int[]{top+h, top, top},3);
                g2.setColor(Color.BLACK);
                g2.fillPolygon(tri);
                g2.dispose();
            }
        };

        bar.setOpaque(false);
        bar.add(tabJeuPanel);
        bar.add(tabAudioPanel);
        bar.add(tabAutresPanel);

        return bar;
    }

    private JPanel createJeuPanel() {
        JPanel jeuPanel = new JPanel(new GridBagLayout());
        jeuPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel botLabel = new JLabel("Difficulté du bot", SwingConstants.CENTER);
        botLabel.setForeground(Color.WHITE);
        botLabel.setFont(botLabel.getFont().deriveFont(Font.BOLD, 18f));

        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 12, 0);
        jeuPanel.add(botLabel, gbc);

        final Color diffNormal = new Color(200, 60, 60);
        final Color diffHover  = new Color(225, 85, 85);

        JPanel diffPanel = createHoverRoundedPanel(diffNormal, diffHover, 18, 44);
        botDifficultyButton = createTextButton("Facile", 16f);
        diffPanel.add(botDifficultyButton);

        diffPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                botDifficultyButton.doClick();
                e.consume();
            }
        });
        botDifficultyButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                diffPanel.dispatchEvent(new MouseEvent(
                    diffPanel, MouseEvent.MOUSE_ENTERED,
                    e.getWhen(), e.getModifiersEx(),
                    0, 0, 0, false
                ));
            }
            @Override public void mouseExited(MouseEvent e) {
                diffPanel.dispatchEvent(new MouseEvent(
                    diffPanel, MouseEvent.MOUSE_EXITED,
                    e.getWhen(), e.getModifiersEx(),
                    0, 0, 0, false
                ));
            }
        });

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 60, 0, 60); 
        jeuPanel.add(diffPanel, gbc);

        return jeuPanel;
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
        soundLabel.setFont(soundLabel.getFont().deriveFont(Font.BOLD, 18f));

        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 12, 0);
        audioPanel.add(soundLabel, gbc);

        final Color sNormal = new Color(200, 60, 60);
        final Color sHover  = new Color(225, 85, 85);

        JPanel soundPanel = createHoverRoundedPanel(sNormal, sHover, 18, 44);
        soundButton = createTextButton("ON", 16f);
        soundPanel.add(soundButton);

        soundPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                soundButton.doClick();
                e.consume();
            }
        });

        soundButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                soundPanel.dispatchEvent(new MouseEvent(
                    soundPanel, MouseEvent.MOUSE_ENTERED,
                    e.getWhen(), e.getModifiersEx(),
                    0, 0, 0, false
                ));
            }
            @Override public void mouseExited(MouseEvent e) {
                soundPanel.dispatchEvent(new MouseEvent(
                    soundPanel, MouseEvent.MOUSE_EXITED,
                    e.getWhen(), e.getModifiersEx(),
                    0, 0, 0, false
                ));
            }
        });

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 60, 0, 60);
        audioPanel.add(soundPanel, gbc);

        //Contrôle volume
        JLabel volLabel = new JLabel("Volume", SwingConstants.CENTER);
        volLabel.setForeground(Color.WHITE);
        volLabel.setFont(volLabel.getFont().deriveFont(Font.BOLD, 18f));

        gbc.gridy = 2;
        gbc.insets = new Insets(28, 0, 12, 0);
        audioPanel.add(volLabel, gbc);

        JPanel volumeRow = new JPanel(new GridBagLayout());
        volumeRow.setOpaque(false);

        GridBagConstraints g2 = new GridBagConstraints();
        g2.gridy = 0;
        g2.fill = GridBagConstraints.HORIZONTAL;

        final Color aNormal = new Color(200, 60, 60);
        final Color aHover  = new Color(225, 85, 85);

        //Fleche gauche (hover)
        JPanel leftPanel = createHoverRoundedPanel(aNormal, aHover, 18, 44);
        leftPanel.setPreferredSize(new Dimension(60, 44));
        volumeLeftButton = createTextButton("<", 18f);
        leftPanel.add(volumeLeftButton);

        leftPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                volumeLeftButton.doClick();
                e.consume();
            }
        });

        volumeLeftButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                leftPanel.dispatchEvent(new MouseEvent(
                    leftPanel, MouseEvent.MOUSE_ENTERED,
                    e.getWhen(), e.getModifiersEx(),
                    0, 0, 0, false
                ));
            }
            @Override public void mouseExited(MouseEvent e) {
                leftPanel.dispatchEvent(new MouseEvent(
                    leftPanel, MouseEvent.MOUSE_EXITED,
                    e.getWhen(), e.getModifiersEx(),
                    0, 0, 0, false
                ));
            }
        });

        g2.gridx = 0;
        g2.weightx = 0;
        g2.insets = new Insets(0, 0, 0, 12);
        volumeRow.add(leftPanel, g2);

        //Valeur volume (sans hover)
        JPanel valuePanel = new JPanel(new GridBagLayout());
        valuePanel.setOpaque(false);
        valuePanel.setPreferredSize(new Dimension(120, 44));

        volumeValueButton = createTextButton("10", 16f);
        valuePanel.add(volumeValueButton);

        g2.gridx = 1;
        g2.weightx = 1;
        g2.insets = new Insets(0, 0, 0, 0);
        volumeRow.add(valuePanel, g2);

        //Fleche droite (hover)
        JPanel rightPanel = createHoverRoundedPanel(aNormal, aHover, 18, 44);
        rightPanel.setPreferredSize(new Dimension(60, 44));
        volumeRightButton = createTextButton(">", 18f);
        rightPanel.add(volumeRightButton);

        rightPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                volumeRightButton.doClick();
                e.consume();
            }
        });

        volumeRightButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                rightPanel.dispatchEvent(new MouseEvent(
                    rightPanel, MouseEvent.MOUSE_ENTERED,
                    e.getWhen(), e.getModifiersEx(),
                    0, 0, 0, false
                ));
            }
            @Override public void mouseExited(MouseEvent e) {
                rightPanel.dispatchEvent(new MouseEvent(
                    rightPanel, MouseEvent.MOUSE_EXITED,
                    e.getWhen(), e.getModifiersEx(),
                    0, 0, 0, false
                ));
            }
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
        fsLabel.setFont(fsLabel.getFont().deriveFont(Font.BOLD, 18f));

        gbcA.gridy = 0;
        gbcA.insets = new Insets(20, 0, 12, 0);
        autresPanel.add(fsLabel, gbcA);

        final Color fsNormal = new Color(200, 60, 60);
        final Color fsHover  = new Color(225, 85, 85);

        JPanel fullScreenPanel = createHoverRoundedPanel(fsNormal, fsHover, 18, 44);
        fullscreenButton = createTextButton("OFF", 16f);
        fullScreenPanel.add(fullscreenButton);

        fullScreenPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                fullscreenButton.doClick();
                e.consume();
            }
        });

        fullscreenButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                fullScreenPanel.dispatchEvent(new MouseEvent(
                    fullScreenPanel, MouseEvent.MOUSE_ENTERED,
                    e.getWhen(), e.getModifiersEx(),
                    0, 0, 0, false
                ));
            }
            @Override public void mouseExited(MouseEvent e) {
                fullScreenPanel.dispatchEvent(new MouseEvent(
                    fullScreenPanel, MouseEvent.MOUSE_EXITED,
                    e.getWhen(), e.getModifiersEx(),
                    0, 0, 0, false
                ));
            }
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
        b.setFont(b.getFont().deriveFont(Font.BOLD, fontSize));
        return b;
    }

    // Panneau rond avec hover
    private JPanel createHoverRoundedPanel(Color normal, Color hover, int arc, int height) {
        final Color[] current = { normal };
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current[0]);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
                g2.dispose();
            }
        };

        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, height));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        MouseAdapter hoverListener = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e){
                current[0] = hover;
                p.repaint();
            }
            @Override public void mouseExited(MouseEvent e){
                current[0] = normal;
                p.repaint(); 
            }
        };
        p.addMouseListener(hoverListener);
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
        b.setFont(b.getFont().deriveFont(Font.BOLD, 16f));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    //Fait des "tabs" (zone extendue des jbutton) cliquable 
    private JPanel createTab(JButton tabButton,Color normal,Color hover,int arc,int height) {
        JPanel tabPanel = createHoverRoundedPanel(normal, hover, arc, height);
        tabPanel.add(tabButton);
        // Hover quand la souris est sur le texte
        tabButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                tabPanel.dispatchEvent(new MouseEvent(
                    tabPanel, MouseEvent.MOUSE_ENTERED,
                    e.getWhen(), e.getModifiersEx(),
                    0, 0, 0, false
                ));
            }
            @Override public void mouseExited(MouseEvent e) {
                tabPanel.dispatchEvent(new MouseEvent(
                    tabPanel, MouseEvent.MOUSE_EXITED,
                    e.getWhen(), e.getModifiersEx(),
                    0, 0, 0, false
                ));
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
    public JButton getBackButton(){return backButton;}
    public JPanel getContentPanel(){return contentPanel;}
    public CardLayout getCardLayout(){return (CardLayout) contentPanel.getLayout();}
    public JButton getTabJeu(){ return tabJeu;}
    public JButton getTabAudio(){ return tabAudio;}
    public JButton getTabAutres(){ return tabAutres;}
    public JPanel getTabJeuPanel() { return tabJeuPanel;}
    public JPanel getTabAudioPanel() { return tabAudioPanel;}
    public JPanel getTabAutresPanel() { return tabAutresPanel;}
    public JButton getBotDifficultyButton() { return botDifficultyButton;}
    public JButton getFullscreenButton() { return fullscreenButton;}
    public JButton getSoundButton() { return soundButton;}
    public JButton getVolumeLeftButton() { return volumeLeftButton;}
    public JButton getVolumeValueButton() { return volumeValueButton;}
    public JButton getVolumeRightButton() { return volumeRightButton;}

    public void setSelectedTabPanel(JComponent tabPanel) {
        this.selectedTabPanel = tabPanel;
        if (tabsBar != null) tabsBar.repaint();
    }
}
