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
    private final JPanel card;
    private final JButton backButton;

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
        backButton = createTextButton("Retour", 18f);
        backPanel.add(backButton);
        
        //Pour toute la bande
        backPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                backButton.doClick();
            }
        });

        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.add(backPanel, BorderLayout.SOUTH);

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

    
    public JButton getBackButton() { return backButton; }
    
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
        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { current[0] = hover;  p.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { current[0] = normal; p.repaint(); }
        });

        return p;
    }
}
