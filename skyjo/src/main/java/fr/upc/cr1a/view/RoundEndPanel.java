package fr.upc.cr1a.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.model.PlayerModel;
import fr.upc.cr1a.utils.ImageCache;

/**
 * Panel affiché entre les manches avec classement des joueurs et bouton continuer/menu
 *
 * @author Ian
 */
public class RoundEndPanel extends JPanel {

    private DoodleButton btnContinuer;
    private DoodleButton btnRetourMenu;
    private PlayerPreviewPanel previewPanel;
    private JLayeredPane layeredPane;
    private boolean gameOver;
    private List<JLabel> playerLabels = new ArrayList<>();
    private List<PlayerModel> sortedPlayers;
    private Image bgImage;
    private BufferedImage cachedBackground;
    private PlayerModel lastRoundPlayer;

    private static Font baseFont;

    public RoundEndPanel(List<PlayerModel> players, boolean gameOver) {
        this(players, gameOver, null);
    }

    public RoundEndPanel(List<PlayerModel> players, boolean gameOver, PlayerModel lastRoundPlayer) {
        this.gameOver = gameOver;
        setLayout(new BorderLayout());
        setOpaque(true);
        bgImage = ImageCache.get("/background.png");

        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        layeredPane.add(contentPanel, JLayeredPane.DEFAULT_LAYER);

        // Preview panel hover
        previewPanel = new PlayerPreviewPanel();
        previewPanel.setVisible(false);
        layeredPane.add(previewPanel, JLayeredPane.PALETTE_LAYER);

        // full screen etc
        final JPanel contentRef = contentPanel;
        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = layeredPane.getSize();
                contentRef.setBounds(0, 0, size.width, size.height);
                previewPanel.setBounds(size.width / 6, size.height / 10, size.width - size.width / 3, size.height - size.height / 5);
                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });

        // haut : titre à gauche, bouton à droite
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(30, 40, 15, 40));

        String title = gameOver ? "Partie terminée !" : "Fin de la manche";
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font(46f, Font.BOLD));
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.WEST);

        if (!gameOver) {
            btnContinuer = new DoodleButton("Continuer", ButtonColor.GREEN, 320, 80, "/Button/Continue.png");
            topBar.add(btnContinuer, BorderLayout.EAST);
        } else {
            btnRetourMenu = new DoodleButton("Menu", ButtonColor.RED, 200, 70);
            topBar.add(btnRetourMenu, BorderLayout.EAST);
        }

        contentPanel.add(topBar, BorderLayout.NORTH);

        // grande boite transparent en bas de topbar
        sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((a, b) -> a.getTotalScore() - b.getTotalScore());

        JPanel bigCard = makeCard();
        bigCard.setLayout(new BoxLayout(bigCard, BoxLayout.Y_AXIS));
        bigCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Label explicatif du calcul
        JLabel scoreExplanation = new JLabel("Calcul : pts dans la main + carte action");
        scoreExplanation.setFont(font(16f, Font.ITALIC));
        scoreExplanation.setForeground(new Color(255, 255, 255, 200));
        bigCard.add(scoreExplanation);
        bigCard.add(Box.createVerticalStrut(12));

        for (int i = 0; i < sortedPlayers.size(); i++) {
            PlayerModel p = sortedPlayers.get(i);
            
            //calculer les détails du score
            int handScore = p.getHand().calculateScore();
            int actionScore = p.getActionHand().size() * 10;
            int totalRoundScore = handScore + actionScore;
            
            String text = (i + 1) + ".   " + p.getName();
            if (lastRoundPlayer != null && p.equals(lastRoundPlayer)) {
                text += " (first)";
            }
            text += "   —   " + handScore + " + " + actionScore + " = " + totalRoundScore + " pts";
            
            // Vérifier si doublement appliqué (si c'est le premier joueur à révéler toutes cartes et qu'il n'a pas le meilleur score)
            if (lastRoundPlayer != null && p.equals(lastRoundPlayer)) {
                // Chercher si ce joueur n'est pas le premier de la liste (meilleur score)
                if (i > 0) {
                    int finalScore = totalRoundScore * 2;
                    text += " × 2 = " + finalScore + " pts";
                }
            }
            
            JLabel pill = makePill(text);
            playerLabels.add(pill);
            bigCard.add(pill);
            bigCard.add(Box.createVerticalStrut(14));
        }

        JPanel cardPadding = new JPanel(new BorderLayout());
        cardPadding.setOpaque(false);
        cardPadding.setBorder(BorderFactory.createEmptyBorder(0, 40, 40, 40));
        cardPadding.add(bigCard, BorderLayout.CENTER);
        contentPanel.add(cardPadding, BorderLayout.CENTER);
    }

    // pilule joueur
    private JLabel makePill(String text) {
        JLabel pill = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setOpaque(false);
        pill.setBackground(Color.WHITE);
        pill.setForeground(new Color(50, 50, 50));
        pill.setFont(font(22f, Font.BOLD));
        pill.setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28));
        pill.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return pill;
    }

    private JPanel makeCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 70));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    // charge le font doodle comme toujours
    private Font font(float size, int style) {
        if (baseFont == null) {
            try {
                var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
                if (stream != null) baseFont = Font.createFont(Font.TRUETYPE_FONT, stream);
            } catch (Exception ignored) {}
        }
        if (baseFont != null) return baseFont.deriveFont(style, size);
        return new Font("Arial", style, (int) size);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            int w = getWidth(), h = getHeight();
            if (cachedBackground == null || cachedBackground.getWidth() != w || cachedBackground.getHeight() != h) {
                cachedBackground = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D bg = cachedBackground.createGraphics();
                bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                bg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                bg.drawImage(bgImage, 0, 0, w, h, null);
                bg.dispose();
            }

            g.drawImage(cachedBackground, 0, 0, null);
        }
    }

    public void showPreview(PlayerModel player) {
        previewPanel.showOtherPlayerHand(player);
        SwingUtilities.invokeLater(() -> previewPanel.fade(true));
    }

    public void hidePreview() {
        previewPanel.fade(false);
    }

    public List<JLabel> getPlayerLabels() {
        return playerLabels;
    }

    public List<PlayerModel> getSortedPlayers() {
        return sortedPlayers;
    }

    public DoodleButton getBtnContinuer() {
        return btnContinuer;
    }

    public DoodleButton getBtnRetourMenu() {
        return btnRetourMenu;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
