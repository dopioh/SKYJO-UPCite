package fr.upc.cr1a.view;

import fr.upc.cr1a.model.ActionCardModel;
import fr.upc.cr1a.model.ActionCardModel.ActionType;
import fr.upc.cr1a.utils.ImageCache;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * Overlay affichant la desc des cartes action
 * Affiche chaque carte avec son image à gauche et sa description à droite
 */
public class ActionCardTutorialOverlayPanel extends JPanel {

    private GameBoardView gameBoardView;
    private float opaque = 0.0f;
    private static final Color SEMI_TRANSPARENT_BG = new Color(0, 0, 0, 100);
    private JPanel contentPanel;

    /**
     * classe interne pour créer des bulles
     */
    private static class PillPanel extends JPanel {
        private Color bgColor;
        
        public PillPanel(Color bgColor) {
            super(new BorderLayout());
            setOpaque(false);
            this.bgColor = bgColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(this.bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            // Bordure arrondie légère pour définir les limites
            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(200, 200, 200, 100));
            g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public ActionCardTutorialOverlayPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setVisible(false);

        //panel principal avec scroll
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        //panel wrapper avec padding et fond arrondi
        JPanel wrapperPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        wrapperPanel.setOpaque(false);
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        //bouton fermer haut à droite
        JButton closeBtn = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fond rouge du jeu
                g2.setColor(new Color(218, 113, 72));
                g2.fillOval(0, 0, getWidth(), getHeight());
                // Croix blanche
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3));
                int pad = 6;
                g2.drawLine(pad, pad, getWidth()-pad, getHeight()-pad);
                g2.drawLine(getWidth()-pad, pad, pad, getHeight()-pad);
                g2.dispose();
            }
        };
        closeBtn.setPreferredSize(new Dimension(35, 35));
        closeBtn.setFont(new Font("Arial", Font.BOLD, 16));
        closeBtn.setOpaque(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> closeOverlay());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Cartes Action");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(closeBtn, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.add(headerPanel, BorderLayout.NORTH);
        contentWrapper.add(scrollPane, BorderLayout.CENTER);

        wrapperPanel.add(contentWrapper, BorderLayout.CENTER);

        add(wrapperPanel, BorderLayout.CENTER);

        //limiter la taille du wrapper
        setPreferredSize(new Dimension(1000, 700));

        //fermer au clic en dehors
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!wrapperPanel.getBounds().contains(e.getPoint())) {
                    closeOverlay();
                }
            }
        });
    }

    public void setGameBoardView(GameBoardView gameBoardView) {
        this.gameBoardView = gameBoardView;
    }

    public void openOverlay() {
        contentPanel.removeAll();
        
        //ajouter toutes les cartes action
        for (ActionType actionType : ActionType.values()) {
            if (actionType == ActionType.DRAW_THREE_CARD) continue; // on a pas encore la description de cette carte, à ajouter plus tard
            JPanel cardPanel = createCardPanel(actionType);
            contentPanel.add(cardPanel);
            contentPanel.add(Box.createVerticalStrut(5));
        }

        contentPanel.add(Box.createVerticalGlue());
        
        setVisible(true);
        opaque = 0.0f;
    }

    private JPanel createCardPanel(ActionType actionType) {
        JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        mainPanel.setOpaque(false);
        mainPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        mainPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 190));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 125, 0, 0));
        // Bulle pour l'image
        PillPanel imagePill = new PillPanel(new Color(255, 255, 255, 230));
        imagePill.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        imagePill.setPreferredSize(new Dimension(160, 175));
        
        String imagePath = getActionImagePath(actionType);
        BufferedImage cardImage = ImageCache.get(imagePath);
        if (cardImage != null) {
            Image scaledImage = cardImage.getScaledInstance(135, 170, Image.SCALE_SMOOTH);
            JLabel cardImageLabel = new JLabel(new ImageIcon(scaledImage));
            cardImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imagePill.add(cardImageLabel, BorderLayout.CENTER);
        }
        mainPanel.add(imagePill);

        // Bulle pour la description
        PillPanel descPill = new PillPanel(new Color(240, 245, 250, 220));
        descPill.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        descPill.setPreferredSize(new Dimension(750, 175));
        
        JPanel descContent = new JPanel();
        descContent.setLayout(new BoxLayout(descContent, BoxLayout.Y_AXIS));
        descContent.setOpaque(false);

        //titre
        JLabel titleLabel = new JLabel(getActionTitle(actionType));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 0, 0));
        descContent.add(titleLabel);
        descContent.add(Box.createVerticalStrut(10));

        //description
        String description = getActionDescription(actionType);
        JTextArea descriptionArea = new JTextArea(description);
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionArea.setForeground(new Color(50, 50, 50));
        descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        descContent.add(descriptionArea);

        descPill.add(descContent, BorderLayout.NORTH);
        mainPanel.add(descPill);

        return mainPanel;
    }

    private String getActionTitle(ActionType actionType) {
        if (gameBoardView != null) {
            return gameBoardView.getActionTitlePublic(actionType);
        }
        return actionType.toString();
    }

    private String getActionDescription(ActionType actionType) {
        if (gameBoardView != null) {
            return gameBoardView.getActionDescriptionPublic(actionType);
        }
        return "Description non disponible";
    }

    private String getActionImagePath(ActionType actionType) {
        switch (actionType) {
            case TRADE_CARD_WITH_PLAYER:
                return "/cartes/carteActionTrade.png";
            case REPLACE_CARD_IN_HAND:
                return "/cartes/carteActionReplace.png";
            case PLAY_TWO_TURN:
                return "/cartes/carteActionTwoTurn.png";
            case DRAW_THREE_CARD:
                return "/cartes/carteActionDraw3.png";
            case PLAY_LAST_ACTION_IN_DEFAUSSE:
                return "/cartes/carteActionReplay.png";
            case ALL_PLAYERS_DROP_CARD:
                return "/cartes/carteActionDrop.png";
            case SPY_ONE_COLUMN:
                return "/cartes/carteActionSpy.png";
            default:
                return "/cartes/carteActionDos.png";
        }
    }

    public void closeOverlay() {
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(SEMI_TRANSPARENT_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.opaque));
        super.paint(g);
        g2.dispose();
    }
}
