package fr.upc.cr1a.view;

import java.awt.*;
import javax.swing.*;

/**
 * Overlay Pour la pause in game, il faudra tout modifié , c'est temporaire donc moche pr le moment
 */
public class PauseOverlayPanel extends JPanel {

    private final JButton btnResume;
    private final JButton btnSettings;
    private final JButton btnQuit;

    public PauseOverlayPanel() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setLayout(new GridLayout(3, 1, 10, 10));
        content.setBackground(new Color(240, 240, 240));
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        btnResume = new JButton("Reprendre");
        btnSettings = new JButton("Paramètres");
        btnQuit = new JButton("Quitter la partie");

        content.add(btnResume);
        content.add(btnSettings);
        content.add(btnQuit);

        add(content);
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