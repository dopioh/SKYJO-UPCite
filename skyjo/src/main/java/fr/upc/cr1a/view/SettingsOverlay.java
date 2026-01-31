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

    public SettingsOverlay() {
        setOpaque(false);
        setVisible(false);
        setLayout(new GridBagLayout()); //pour centrer

        card = new JPanel() {
            //Rectangle arrondi
            @Override
            protected void paintComponent(Graphics g) {
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
        card.setPreferredSize(new Dimension(700, 520));
        card.setBackground(new Color(150, 150, 150));
        card.setOpaque(false);

        //Bouton Fermer le paramtètre
        final Color normal = new Color(90, 120, 140);  //(220, 170, 60) Jaune Balatro si besoin de changer
        final Color hover  = new Color(115, 145, 165); // (245, 195, 85) 

        JPanel backPanel = createHoverRoundedPanel(normal, hover, 20, 55);
        backButton = createTextButton("Fermer", 18f);
        backPanel.add(backButton);
        
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change le curseur 
        backButton.addMouseListener(new MouseAdapter() {
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
                backButton.doClick();
                e.consume(); //Pour éviter le relachement du clic sur le panel principal
            }
        });

        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.add(backPanel, BorderLayout.SOUTH);

        //Catégories paramètres
        final Color tabNormal = new Color(200, 60, 60);
        final Color tabHover  = new Color(225, 85, 85);

        CardLayout cl = (CardLayout) contentPanel.getLayout();

        JPanel tabJeuPanel = createTab(tabJeu, tabNormal, tabHover, 18, 40);
        JPanel tabAudioPanel = createTab(tabAudio, tabNormal, tabHover, 18, 40);
        JPanel tabAutresPanel = createTab(tabAutres, tabNormal, tabHover, 18, 40);
        JPanel tabsBar = new JPanel(new GridLayout(1, 3, 12, 0));

        tabsBar.setOpaque(false);
        tabsBar.add(tabJeuPanel);
        tabsBar.add(tabAudioPanel);
        tabsBar.add(tabAutresPanel);
        tabsBar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        //Parametres jeu
        JPanel jeuPanel = new JPanel();   
        jeuPanel.setOpaque(false);
        jeuPanel.add(new JLabel("Paramètres du jeu"));

        //Parametres Audio
        JPanel audioPanel = new JPanel();  
        audioPanel.setOpaque(false);
        audioPanel.add(new JLabel("Paramètres du audil"));

        //parametres Autres
        JPanel autresPanel = new JPanel(); 
        autresPanel.setOpaque(false);
        autresPanel.add(new JLabel("Paramètres du autres"));

        contentPanel.setOpaque(false);
        contentPanel.add(jeuPanel, "JEU");
        contentPanel.add(audioPanel, "AUDIO");
        contentPanel.add(autresPanel, "AUTRES");

        card.add(tabsBar, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);

        cl.show(contentPanel, "JEU");
        add(card);
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

    //Pour les controllers
    public JButton getBackButton(){return backButton;}
    public JPanel getContentPanel(){return contentPanel;}
    public CardLayout getCardLayout(){return (CardLayout) contentPanel.getLayout();}
    public JButton getTabJeu(){ return tabJeu; }
    public JButton getTabAudio(){ return tabAudio; }
    public JButton getTabAutres(){ return tabAutres; }
    
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
}
