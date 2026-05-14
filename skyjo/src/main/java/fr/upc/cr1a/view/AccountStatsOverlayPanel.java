package fr.upc.cr1a.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.model.player.PlayerAccount;
import fr.upc.cr1a.model.player.PlayerData;

/**
 * Overlay des stats
 * @author Nassim
 */
public class AccountStatsOverlayPanel extends JPanel {

    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 140);
    private static final Color CARD_COLOR = new Color(248, 243, 226, 255);
    private static final Color CARD_BORDER_COLOR = new Color(54, 45, 34, 215);
    private static final Color TITLE_COLOR = new Color(46, 38, 26);
    private static final Color ROW_COLOR = new Color(255, 255, 255, 145);
    private static final Color LABEL_COLOR = new Color(67, 56, 42);
    private static final Color VALUE_COLOR = new Color(38, 33, 25);
    private static final Color INFO_COLOR = new Color(75, 64, 50);

    private final DoodleButton closeButton;
    private final DoodleButton disconnectButton;
    private final Font titleFont;
    private final Font labelFont;
    private final Font valueFont;
    private final Font infoFont;
    private final Frame frame;

    public static String getRealRankName(int rankId) {
        switch (rankId) {
            case 0:
                return "Debutant";
            case 1:
                return "Noob";
            case 2:
                return "Apprenti";
            case 3:
                return "Tryhard";
            case 4:
                return "As du jeu";
            case 5:
                return "Genie du skyjo";
            case 6:
                return "Maitre du skyjo";
            case 7:
                return "Grand-Maitre du skyjo";
            case 8:
                return "GOAT du skyjo";
            case 9:
                return "TRICHEUR";
            default:
                return "Inconnu";
        }
    }

    public AccountStatsOverlayPanel(Frame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(null);
        setVisible(false);

        titleFont = loadFont(44f, Font.BOLD);
        labelFont = loadFont(24f, Font.BOLD);
        valueFont = loadFont(24f, Font.PLAIN);
        infoFont = loadFont(20f, Font.PLAIN);

        closeButton = new DoodleButton("Fermer", ButtonColor.RED, 180, 58);
        closeButton.setSfxPath("/sfx/back.wav");
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.onMouseEnter();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.onMouseExit();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                closeButton.onMouseExit();
                setVisible(false);
                repaint();
            }
        });
        add(closeButton);

        disconnectButton = new DoodleButton("Déconnexion", ButtonColor.RED, 180, 58);
        disconnectButton.setFontSize(28f);
        disconnectButton.setSfxPath("/sfx/back.wav");
        disconnectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                disconnectButton.onMouseEnter();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                disconnectButton.onMouseExit();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                disconnectButton.onMouseExit();
                handleDisconnect();
            }
        });
        add(disconnectButton);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutButtons();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                layoutButtons();
            }
        });
    }

    public void openStats() {
        setVisible(true);
        closeButton.setEnabled(true);
        closeButton.onMouseExit();
        
        // Afficher le bouton de déconnexion seulement si connecté (pas en invité/null)
        boolean isConnected = PlayerAccount.actuelAccount != null;
        disconnectButton.setEnabled(isConnected);
        disconnectButton.setVisible(isConnected);
        if (isConnected) {
            disconnectButton.onMouseExit();
        }
        
        layoutButtons();
        repaint();
    }

    public boolean isOpen() {
        return isVisible();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!isVisible()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        drawOverlayBackground(g2);
        Rectangle card = getCardBounds();
        drawCard(g2, card);
        drawTitle(g2, card);
        drawStats(g2, card);

        g2.dispose();
    }

    private void drawOverlayBackground(Graphics2D g2) { // fond sombre en dehors de l'overlay
        g2.setColor(OVERLAY_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawCard(Graphics2D g2, Rectangle card) { // l'overlay des stats
        g2.setColor(CARD_COLOR);
        g2.fillRoundRect(card.x, card.y, card.width, card.height, 24, 24);
        g2.setColor(CARD_BORDER_COLOR);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(card.x, card.y, card.width - 1, card.height - 1, 24, 24);
    }

    private void drawTitle(Graphics2D g2, Rectangle card) {
        g2.setColor(TITLE_COLOR);
        g2.setFont(titleFont);
        g2.drawString("Statistiques du compte", card.x + 28, card.y + 54);
    }

    private void drawStats(Graphics2D g2, Rectangle card) {
        PlayerData account = PlayerAccount.actuelAccount;
        if (account == null) {
            drawNoAccountMessage(g2, card);
            return;
        }

        String[] labels = { "Pseudo", "Rang", "Elo", "Argent"};
        String[] values = {
            safeText(account.getUsername(), "Inconnu"),
            getRealRankName(account.getRankId()),
            String.valueOf(account.getElo()),
            account.getMoney() + " $"
        };

        int left = card.x + 34;
        int width = card.width - 68;
        int rowHeight = 54;
        int rowGap = 10;
        int firstRowY = card.y + 98;

        for (int i = 0; i < labels.length; i++) {
            int y = firstRowY + i * (rowHeight + rowGap);
            drawStatRow(g2, labels[i], values[i], left, y, width, rowHeight);
        }

        String idText = "ID: " + safeText(account.getID(), "N/A");
        g2.setColor(INFO_COLOR);
        g2.setFont(infoFont);
        g2.drawString(idText, left + 8, card.y + card.height - 24);
    }

    private void drawNoAccountMessage(Graphics2D g2, Rectangle card) {
        int textX = card.x + 48;
        g2.setColor(INFO_COLOR);
        g2.setFont(valueFont);
        g2.drawString("Aucun compte connecte.", textX, card.y + card.height / 2 - 10);
        g2.setFont(infoFont);
        g2.drawString("Connectez-vous depuis l'ecran de connexion pour afficher vos statistiques.", textX, card.y + card.height / 2 + 30);
    }

    private void drawStatRow(Graphics2D g2, String label, String value, int x, int y, int width, int height) {
        g2.setColor(ROW_COLOR);
        g2.fillRoundRect(x, y, width, height, 16, 16);

        g2.setFont(labelFont);
        g2.setColor(LABEL_COLOR);
        FontMetrics labelMetrics = g2.getFontMetrics();
        int baselineY = y + (height - labelMetrics.getHeight()) / 2 + labelMetrics.getAscent();
        g2.drawString(label + ":", x + 18, baselineY);

        g2.setFont(valueFont);
        g2.setColor(VALUE_COLOR);
        FontMetrics valueMetrics = g2.getFontMetrics();
        int valueX = x + width - 18 - valueMetrics.stringWidth(value);
        int valueY = y + (height - valueMetrics.getHeight()) / 2 + valueMetrics.getAscent();
        g2.drawString(value, valueX, valueY);
    }

    private Rectangle getCardBounds() {
        int baseW = 760;
        int baseH = 500;

        int cardW = Math.min(getWidth() - 120, baseW);
        int cardH = Math.min(getHeight() - 100, baseH);

        cardW = Math.max(cardW, 620);
        cardH = Math.max(cardH, 420);

        if (cardW > getWidth() - 30) {
            cardW = Math.max(320, getWidth() - 30);
        }
        if (cardH > getHeight() - 30) {
            cardH = Math.max(240, getHeight() - 30);
        }

        int x = (getWidth() - cardW) / 2;
        int y = (getHeight() - cardH) / 2;
        return new Rectangle(x, y, cardW, cardH);
    }

    private void layoutButtons() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        Rectangle card = getCardBounds();
        Dimension closeButtonSize = closeButton.getPreferredSize();
        Dimension disconnectButtonSize = disconnectButton.getPreferredSize();
        
        // Placer le bouton "Fermer" en haut à droite
        closeButton.setBounds(
            card.x + card.width - closeButtonSize.width - 18,
            card.y + 14,
            closeButtonSize.width,
            closeButtonSize.height
        );
        
        // Placer le bouton "Déconnexion" en bas à droite
        disconnectButton.setBounds(
            card.x + card.width - disconnectButtonSize.width - 18,
            card.y + card.height - disconnectButtonSize.height - 18,
            disconnectButtonSize.width,
            disconnectButtonSize.height
        );
    }

    private void handleDisconnect() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Êtes-vous sûr de vouloir vous déconnecter ?",
            "Confirmation de déconnexion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Déconnecter le joueur
            PlayerAccount.actuelAccount = null;
            
            // Fermer l'overlay
            setVisible(false);
            
            // Rediriger vers l'écran de connexion
            if (frame != null) {
                frame.showPanel("CONNEXION");
            }
            
            repaint();
        }
    }

    private Font loadFont(float size, int style) {
        try (InputStream stream = getClass().getResourceAsStream("/PatrickHandSC.ttf")) {
            if (stream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(style, size);
            }
            return new Font("Arial", style, Math.round(size));
        } catch (FontFormatException | IOException ignored) {
            return new Font("Arial", style, Math.round(size));
        }
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
