package fr.upc.cr1a.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

/**
 * Overlay Pour la pause in game
 */
public class PauseOverlayPanel extends JPanel {

    private final JButton btnResume;
    private final JButton btnSettings;
    private final JButton btnQuit;
    private Font doodleFont;

    public PauseOverlayPanel() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        // police doodle
        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            doodleFont = Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (Exception e) {
            doodleFont = new Font("Arial", Font.BOLD, 12);
        }

        JPanel card = createCardPanel();
        card.setBackground(new Color(80, 168, 74));
        card.setPreferredSize(new Dimension(420, 380));
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Pause", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(doodleFont.deriveFont(Font.BOLD, 40f));
        card.add(title, BorderLayout.NORTH);

        final Color green = new Color(65, 140, 58);
        final Color greenHover = new Color(52, 115, 47);
        final Color red = new Color(140, 103, 58);
        final Color redHover = new Color(113, 82, 45);

        String[] labels  = { "Reprendre", "Paramètres", "Quitter la partie" };
        Color[] normals = { green, green, red };
        Color[] hovers  = { greenHover, greenHover, redHover };
        JButton[] buttons = new JButton[3];

        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 0, 12));
        buttonsPanel.setOpaque(false);

        for (int i = 0; i < 3; i++) {
            JPanel p = createHoverRoundedPanel(normals[i], hovers[i], 18, 52);
            buttons[i] = createTextButton(labels[i], 22f);
            p.add(buttons[i]);
            addListeners(p, buttons[i], normals[i], hovers[i]);
            buttonsPanel.add(p);
        }

        btnResume = buttons[0];
        btnSettings = buttons[1];
        btnQuit = buttons[2];

        card.add(buttonsPanel, BorderLayout.CENTER);
        add(card);
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private JPanel createCardPanel() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 50, 50));
                // bordure
                g2.setColor(new Color(0, 0, 0, 64));
                g2.setStroke(new BasicStroke(5));
                g2.draw(new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 50, 50));
                g2.dispose();
            }

        };
        p.setOpaque(false);
        return p;
    }

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

    private JButton createTextButton(String text, float fontSize) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(Color.WHITE);
        b.setFont(doodleFont.deriveFont(Font.BOLD, fontSize));
        return b;
    }

    private void addListeners(JPanel panel, JButton button, Color normal, Color hover) {
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { button.doClick(); }
        });
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { panel.setBackground(hover);  panel.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { panel.setBackground(normal); panel.repaint(); }
        });
    }

    public JButton getBtnResume() {
        return btnResume;
    }

    public JButton getBtnSettings() {
        return btnSettings;
    }

    public JButton getBtnQuit() {
        return btnQuit;
    }
}