package fr.upc.cr1a.view;

import fr.upc.cr1a.model.AnimLoop;
import fr.upc.cr1a.model.player.PlayerData;
import fr.upc.cr1a.utils.ImageCache;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Overlay classement des joueurs
 */
public class Leaderboard extends JComponent {

    private static final Color ROW_1 = new Color(90, 180, 84);
    private static final Color ROW_2 = new Color(105, 190, 99);
    private static final Color COLOR_1ST = new Color(241, 192, 31);
    private static final Color COLOR_2ND = new Color(214, 207, 207);
    private static final Color COLOR_3RD = new Color(223, 175, 92);
    private static final Color BTN_CLOSE = new Color(72, 129, 64);

    private JLabel backButton;
    private JPanel listPanel;
    private Font doodleFont;
    private boolean loading = false;
    private float loadingAlpha = 0.5f;
    private float loadingTime = 0f;

    public Leaderboard() {
        setOpaque(false);
        setVisible(false);
        setLayout(new GridBagLayout());

        // charger la police doodle comme toujours
        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            doodleFont = Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (Exception e) {
            doodleFont = new Font("Arial", Font.BOLD, 12);
        }

        // carte verte arrondie principale
        JPanel card = createCard();
        card.setPreferredSize(new Dimension(620, 560));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        // barre du titre avec icone podium - titre - croix
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        Image podiumImg = ImageCache.get("/podium.png").getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        JLabel podiumIcon = new JLabel(new ImageIcon(podiumImg));

        JLabel title = new JLabel("Classement");
        title.setForeground(Color.WHITE);
        title.setFont(doodleFont.deriveFont(Font.BOLD, 36f));
        title.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        JPanel titleLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleLeft.setOpaque(false);
        titleLeft.add(podiumIcon);
        titleLeft.add(title);

        // pas JButton pour eviter "..."
        backButton = new JLabel("X", SwingConstants.CENTER);
        backButton.setFont(doodleFont.deriveFont(Font.BOLD, 40f));
        backButton.setForeground(BTN_CLOSE);
        backButton.setPreferredSize(new Dimension(46, 46));
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        titleBar.add(titleLeft, BorderLayout.CENTER);
        titleBar.add(backButton, BorderLayout.EAST);
        card.add(titleBar, BorderLayout.NORTH);

        // liste scrollable
        listPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!loading) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int alpha = (int) (loadingAlpha * 255);
                int rowHeight = 68;
                int gap = 6;
                g2.setColor(new Color(90, 180, 84, alpha));
                for (int i = 0; i < 5; i++) {
                    g2.fillRoundRect(0, i * (rowHeight + gap), getWidth() - 8, rowHeight, 16, 16);
                }
                g2.dispose();
            }
        };
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8)); // espace entre la barre scroll

        JScrollPane scroll = new JScrollPane(listPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16); //vitesse de scroll sinon elle est giga lent par defaut
        card.add(scroll, BorderLayout.CENTER);

        add(card);
    }

    // animation pendant le chargement
    public void showLoading() {
        loading = true;
        loadingTime = 0f;
        listPanel.removeAll();
        listPanel.revalidate();
        listPanel.repaint();
        AnimLoop.add(this, dt -> {
            if (!loading) {
                AnimLoop.remove(this);
                return;
            }
            loadingTime += 0.05f * (float) dt;
            loadingAlpha = (float) Math.abs(Math.sin(loadingTime));
            listPanel.repaint();
        });
    }

    // affiche la liste des joueurs
    public void updatePlayers(List<PlayerData> players) {
        loading = false;
        // tri du plus haut elo au plus bas
        ArrayList<PlayerData> sorted = new ArrayList<>(players);
        for (int i = 0; i < sorted.size() - 1; i++) {
            for (int j = 0; j < sorted.size() - 1 - i; j++) {
                if (sorted.get(j).getElo() < sorted.get(j + 1).getElo()) {
                    PlayerData tmp = sorted.get(j);
                    sorted.set(j, sorted.get(j + 1));
                    sorted.set(j + 1, tmp);
                }
            }
        }

        ImageIcon avatar = new ImageIcon(ImageCache.get("/Account.png").getScaledInstance(38, 38, Image.SCALE_SMOOTH));

        listPanel.removeAll(); // rafraichi pour pas avoir des doublons 

        for (int i = 0; i < sorted.size(); i++) {
            PlayerData p = sorted.get(i);
            int rank = i + 1;

            Color rowBg;
            if (i % 2 == 0) {
                rowBg = ROW_1;
            } else {
                rowBg = ROW_2;
            }
            JPanel row = createRoundedPanel(rowBg);
            row.setLayout(new BorderLayout(10, 0));
            row.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

            // numero de rang
            JLabel rankLabel = new JLabel(String.valueOf(rank));
            rankLabel.setFont(doodleFont.deriveFont(Font.BOLD, 28f));
            rankLabel.setPreferredSize(new Dimension(36, 36));
            rankLabel.setHorizontalAlignment(SwingConstants.CENTER);
            if (rank == 1) rankLabel.setForeground(COLOR_1ST);
            else if (rank == 2) rankLabel.setForeground(COLOR_2ND);
            else if (rank == 3) rankLabel.setForeground(COLOR_3RD);
            else rankLabel.setForeground(Color.WHITE);
            row.add(rankLabel, BorderLayout.WEST);

            // avatar + nom
            JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            center.setOpaque(false);
            center.add(new JLabel(avatar));
            String name = p.getUsername();
            if (name.length() > 24) { // max 24 caractere
                name = name.substring(0, 24) + "...";
            }
            JLabel nameLabel = new JLabel(name);
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setFont(doodleFont.deriveFont(Font.BOLD, 22f));
            center.add(nameLabel);
            row.add(center, BorderLayout.CENTER);

            // rank + elo
            JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            east.setOpaque(false);
            JPanel rankBadge = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(219, 240, 217));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                }
            };
            rankBadge.setOpaque(false);
            rankBadge.setLayout(new GridBagLayout());
            rankBadge.setPreferredSize(new Dimension(36, 26));
            JLabel rankLabel2 = new JLabel(String.valueOf(p.getRankId()), SwingConstants.CENTER);
            rankLabel2.setForeground(new Color(80, 168, 74));
            rankLabel2.setFont(doodleFont.deriveFont(Font.BOLD, 16f));
            rankBadge.add(rankLabel2, new GridBagConstraints());
            east.add(rankBadge);
            JLabel scoreLabel = new JLabel(p.getElo() + " elo");
            scoreLabel.setForeground(Color.WHITE);
            scoreLabel.setFont(doodleFont.deriveFont(Font.BOLD, 20f));
            east.add(scoreLabel);
            row.add(east, BorderLayout.EAST);

            listPanel.add(row);
            listPanel.add(Box.createVerticalStrut(6));
        }

        if (sorted.isEmpty()) {
            JLabel empty = new JLabel("Aucun joueur", SwingConstants.CENTER);
            empty.setForeground(Color.WHITE);
            empty.setFont(doodleFont.deriveFont(Font.BOLD, 16f));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(empty);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    // fond transparent quand l'overlay est ouvert merci hugo
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private JPanel createCard() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(80, 168, 74));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 50, 50));
                g2.setColor(new Color(0, 0, 0, 64));
                g2.setStroke(new BasicStroke(5));
                g2.draw(new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 50, 50));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JPanel createRoundedPanel(Color bg) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    public JLabel getBackButton() { return backButton; }
}
