package fr.upc.cr1a.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Overlay pour afficher les contacts 
 */
public class ContactsOverlay extends JComponent {
    
    private final JPanel card;
    private Font doodleFont;
    private final JLabel closeButton;
    private final JTextArea textArea;

    public ContactsOverlay() {
        setOpaque(false);
        setVisible(false);
        setLayout(new GridBagLayout());

        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            doodleFont = Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (Exception e) {
            doodleFont = new Font("Arial", Font.BOLD, 12);
        }

        // carte fond vert arrondi pareil que Ian
        card = createCardPanel();
        card.setPreferredSize(new Dimension(600, 400));
        card.setBackground(new Color(80, 168, 74)); // Le meme vert que SettingsOverlay
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel qui gere le titre et la croix

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);


        closeButton = new JLabel("X", SwingConstants.CENTER);
        closeButton.setFont(doodleFont.deriveFont(Font.BOLD, 40f));
        closeButton.setForeground(new Color(72, 129, 64)); // Vert foncé
        //closeButton.setForeground(new Color(92, 130, 59));
        closeButton.setPreferredSize(new Dimension(46, 46));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topBar.add(closeButton, BorderLayout.WEST); 

        // Titre
        JLabel titleLabel = new JLabel("Contacts", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(doodleFont.deriveFont(Font.BOLD, 36f));
        topBar.add(titleLabel, BorderLayout.CENTER);

        // ZONE TEXTeE
        textArea = new JTextArea();
        textArea.setText("Pour toutes questions, demandes, réclamations, ou report un joueur pour X raison.\n"
                        + "Veuillez contacter le mail suivant à partir du même mail que vous utilisez pour votre compte Skyjo.\n\n"
                        + "Email : contacts.skyjo@gmail.com");
        textArea.setFont(doodleFont.deriveFont(Font.PLAIN, 24f));
        textArea.setForeground(Color.WHITE);
        textArea.setOpaque(false); 
        textArea.setEditable(false); // empeche le joueur d'ecrire dedans
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setMargin(new Insets(20, 20, 20, 20));


        card.add(topBar, BorderLayout.NORTH);
        card.add(textArea, BorderLayout.CENTER);

        add(card);

        // redimensionnement adaptatif
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateCardBounds();
            }
        });
        updateCardBounds();
    }

    /**
     * Peint le fond semi-transparent noir derrière l'overlay
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    /**
     * Crée le panneau avec les bords arrondis et la bordure
     */
    private JPanel createCardPanel() {
        return new JPanel() {
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

    /**
     * Gère le redimmentionnement
     */
    private void updateCardBounds() {
        int w = getWidth();
        int h = getHeight();
        int cardW = (int) (w * 0.50);
        int cardH = (int) (h * 0.60);
        cardW = Math.max(500, Math.min(cardW, 900));
        cardH = Math.max(350, Math.min(cardH, 600));
        card.setPreferredSize(new Dimension(cardW, cardH));
        card.revalidate();
        card.repaint();
    }

    // getter
    public JLabel getCloseButton() { return closeButton; }
}