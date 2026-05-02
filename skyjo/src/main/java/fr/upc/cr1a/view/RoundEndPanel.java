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

    // Encadré stats (uniquement en game over) : ELO / XP / Money + deltas
    private JPanel statsCard;
    private JLabel statsTitleLabel;
    private JLabel eloLabel;
    private JLabel xpLabel;
    private JLabel moneyLabel;
    private JLabel statusLabel;

    private static Font baseFont;

    // Pré-remplissage du card stats avec les valeurs locales
    private String localUsername;
    private int initialElo;
    private int initialXp;
    private int initialMoney;

    public RoundEndPanel(List<PlayerModel> players, boolean gameOver) {
        this(players, gameOver, null);
    }

    public RoundEndPanel(List<PlayerModel> players, boolean gameOver, PlayerModel lastRoundPlayer) {
        // On lit le PlayerAccount local (chque machine) pour personnaliser l'affichage
        if (fr.upc.cr1a.model.player.PlayerAccount.actuelAccount != null) {
            this.localUsername  = fr.upc.cr1a.model.player.PlayerAccount.actuelAccount.getUsername();
            this.initialElo     = fr.upc.cr1a.model.player.PlayerAccount.actuelAccount.getElo();
            this.initialXp      = fr.upc.cr1a.model.player.PlayerAccount.actuelAccount.getXp();
            this.initialMoney   = fr.upc.cr1a.model.player.PlayerAccount.actuelAccount.getMoney();
        } else {
            this.localUsername  = "Invité";
            this.initialElo     = 0;
            this.initialXp      = 0;
            this.initialMoney   = 0;
        }
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

        // Encadré stats sous le titre, uniquement en fin de partie
        if (gameOver) {
            JPanel northBlock = new JPanel();
            northBlock.setOpaque(false);
            northBlock.setLayout(new BoxLayout(northBlock, BoxLayout.Y_AXIS));
            northBlock.add(topBar);
            northBlock.add(buildStatsCard());
            contentPanel.add(northBlock, BorderLayout.NORTH);
        } else {
            contentPanel.add(topBar, BorderLayout.NORTH);
        }

        // full screen etc
        final JPanel contentRef = contentPanel;
        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = layeredPane.getSize();
                contentRef.setBounds(0, 0, size.width, size.height);
                int pw = size.width * 2 / 3;
                int ph = size.height * 3 / 4;
                previewPanel.setBounds((size.width - pw) / 2, (size.height - ph) / 2, pw, ph);
                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });

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
            
            //vérifier si doublement appliqué
            if (lastRoundPlayer != null && p.equals(lastRoundPlayer)) {
                //chercher si ce joueur n'est pas le premier de la liste
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

        JScrollPane scroll = new JScrollPane(bigCard);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        // scroll vertical si besoin mais jamais horizontal
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16); //vitesse de scroll sinon elle est giga lent par defaut

        JPanel cardPadding = new JPanel(new BorderLayout());
        cardPadding.setOpaque(false);
        cardPadding.setBorder(BorderFactory.createEmptyBorder(0, 40, 40, 40));
        cardPadding.add(scroll, BorderLayout.CENTER);
        contentPanel.add(cardPadding, BorderLayout.CENTER);
    }

    private JPanel buildStatsCard() {
        statsCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 110));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        statsCard.setOpaque(false);
        statsCard.setLayout(new BoxLayout(statsCard, BoxLayout.Y_AXIS));
        statsCard.setBorder(BorderFactory.createEmptyBorder(16, 32, 16, 32));
        JPanel padded = new JPanel(new BorderLayout());
        padded.setOpaque(false);
        padded.setBorder(BorderFactory.createEmptyBorder(0, 40, 10, 40));
        padded.add(statsCard, BorderLayout.CENTER);

        // Titre personnalisé local
        statsTitleLabel = new JLabel("Récompenses de " + localUsername);
        statsTitleLabel.setFont(font(28f, Font.BOLD));
        statsTitleLabel.setForeground(Color.WHITE);
        statsTitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        statsCard.add(statsTitleLabel);
        statsCard.add(Box.createVerticalStrut(8));

        eloLabel   = makeStatLine("ELO",      String.valueOf(initialElo),   "x");
        xpLabel    = makeStatLine("XP",       String.valueOf(initialXp),    "x");
        moneyLabel = makeStatLine("Pièces",   String.valueOf(initialMoney), "x");
        statsCard.add(eloLabel);
        statsCard.add(Box.createVerticalStrut(4));
        statsCard.add(xpLabel);
        statsCard.add(Box.createVerticalStrut(4));
        statsCard.add(moneyLabel);

        statusLabel = new JLabel("Calcul en cours…");
        statusLabel.setFont(font(16f, Font.ITALIC));
        statusLabel.setForeground(new Color(255, 255, 255, 180));
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        statsCard.add(Box.createVerticalStrut(6));
        statsCard.add(statusLabel);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(padded, BorderLayout.CENTER);
        return wrap;
    }

    private JLabel makeStatLine(String name, String value, String delta) {
        JLabel l = new JLabel(formatStatHtml(name, value, delta));
        l.setFont(font(20f, Font.BOLD));
        l.setForeground(Color.WHITE);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private String formatStatHtml(String name, String value, String delta) {
        // utilise du HTML pour la couleur du delta (vert / rouge / gris pour placeholder)
        String deltaPart = "";
        if (delta != null && !delta.isEmpty()) {
            String color;
            if (delta.startsWith("-")) {
                color = "#ff7878"; // rouge si perte
            } else if (delta.startsWith("+")) {
                color = "#7CFC8E"; // vert si gain
            } else {
                color = "#cfd8e0"; // neutre
            }
            deltaPart = " <font color='" + color + "'>(" + delta + ")</font>";
        }
        return "<html><span style='color:#cfd8e0'>" + name + " :</span> " + "<span style='color:white'>" + value + "</span>" + deltaPart + "</html>";
    }

    public void updateStats(int elo, int xp, int money, int deltaElo, int deltaXp, int deltaMoney) {
        if (eloLabel == null) return;
        eloLabel.setText(formatStatHtml("ELO", String.valueOf(elo), "x"));
        xpLabel.setText(formatStatHtml("XP", String.valueOf(xp), "x"));
        moneyLabel.setText(formatStatHtml("Pièces", String.valueOf(money), "x"));
        if (statusLabel != null) {
            statusLabel.setText("Mis à jour");
            statusLabel.setForeground(new Color(180, 255, 200, 220));
        }
        revalidate();
        repaint();
    }

    /**
     * Cas d'echec API : affiche un message d'erreur a la place du status.
     */
    public void showStatsError(String message) {
        if (statusLabel == null) return;
        statusLabel.setText(message != null ? message : "Échec de la synchronisation des stats");
        statusLabel.setForeground(new Color(255, 120, 120, 230));
        revalidate();
        repaint();
    }

    private String formatDelta(int d) {
        if (d == 0) return "±0";
        return (d > 0 ? "+" : "") + d;
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
        pill.setFont(font(24f, Font.BOLD));
        pill.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));
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
