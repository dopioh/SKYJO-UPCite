package fr.upc.cr1a.view;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import fr.upc.cr1a.model.PlayerModel;

/**
 * Overlay affiche en maintenant la touche Tab tableau des scores par manche + total.
 */
public class ScoreboardOverlayPanel extends JPanel {

    private static Font baseFont;

    public ScoreboardOverlayPanel(List<PlayerModel> players) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        card.setLayout(new BorderLayout(0, 12));

        // titre
        JLabel title = new JLabel("Scoreboard (Tab)");
        title.setFont(font(28f, Font.BOLD));
        title.setForeground(Color.WHITE);
        card.add(title, BorderLayout.NORTH);

        // tableau : 1 colonne joueur + N colonnes manches + 1 colonne total
        int maxRounds = 0;
        for (PlayerModel p : players) {
            maxRounds = Math.max(maxRounds, p.getRoundScoreHistory().size());
        }

        JPanel grid = new JPanel(new GridLayout(players.size() + 1, maxRounds + 2, 4, 4));
        grid.setOpaque(false);

        // header
        grid.add(makeHeaderCell("Joueur"));
        for (int r = 1; r <= maxRounds; r++) {
            grid.add(makeHeaderCell("M" + r));
        }
        grid.add(makeHeaderCell("Total"));

        // tri par total croissant 
        List<PlayerModel> sorted = new ArrayList<>(players);
        sorted.sort((a, b) -> a.getTotalScore() - b.getTotalScore());

        for (PlayerModel p : sorted) {
            grid.add(makeNameCell(p.getName()));
            ArrayList<Integer> history = p.getRoundScoreHistory();
            for (int r = 0; r < maxRounds; r++) {
                String txt = (r < history.size()) ? String.valueOf(history.get(r)) : "—";
                grid.add(makeValueCell(txt, false));
            }
            grid.add(makeValueCell(String.valueOf(p.getTotalScore()), true));
        }

        card.add(grid, BorderLayout.CENTER);

        JLabel hint = new JLabel("Relache Tab pour fermer");
        hint.setFont(font(14f, Font.ITALIC));
        hint.setForeground(new Color(255, 255, 255, 160));
        card.add(hint, BorderLayout.SOUTH);

        add(card);
    }

    private JLabel makeHeaderCell(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(new Color(180, 200, 220));
        l.setFont(font(16f, Font.BOLD));
        l.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return l;
    }

    private JLabel makeNameCell(String name) {
        JLabel l = new JLabel(name, SwingConstants.LEFT);
        l.setForeground(Color.WHITE);
        l.setFont(font(18f, Font.BOLD));
        l.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return l;
    }

    private JLabel makeValueCell(String text, boolean isTotal) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(isTotal ? new Color(255, 220, 120) : Color.WHITE);
        l.setFont(font(18f, isTotal ? Font.BOLD : Font.PLAIN));
        l.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return l;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // fond semi-opaque sur tout le panel pour assombrir le jeu derriere
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    private static Font font(float size, int style) {
        if (baseFont == null) {
            try {
                var stream = ScoreboardOverlayPanel.class.getResourceAsStream("/PatrickHandSC.ttf");
                if (stream != null) baseFont = Font.createFont(Font.TRUETYPE_FONT, stream);
            } catch (Exception ignored) {}
        }
        if (baseFont != null) return baseFont.deriveFont(style, size);
        return new Font("Arial", style, (int) size);
    }
}
