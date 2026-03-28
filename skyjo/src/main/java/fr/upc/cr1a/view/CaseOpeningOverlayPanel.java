package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.utils.ImageCache;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Overlay ouverture de pack et ouverture du classeur
 */
public class CaseOpeningOverlayPanel extends JPanel {

    private static final int[][] PACK_CHANCES = {
        {80, 15, 5}, // pack 1: 80% commun, 15% rare, 5% legendaire
        {65, 25, 10}, // pack 2: 65% commun, 25% rare, 10% legendaire
        {55, 30, 15} // pack 3: 55% commun, 30% rare, 15% legendaire
    };

    // constantes pour le front
    private static final int RESULT_TILE_WIDTH = 240;
    private static final int RESULT_TILE_HEIGHT = 320;
    private static final int TILE_ARC = 16;
    private static final int CARD_ARC = 24;

    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 140);
    private static final Color CARD_COLOR = new Color(248, 243, 226, 255);
    private static final Color CARD_BORDER_COLOR = new Color(54, 45, 34, 215);
    private static final Color TITLE_COLOR = new Color(46, 38, 26);
    private static final Color RESULT_TEXT_COLOR = new Color(50, 40, 28);
    private static final Color TILE_BORDER_COLOR = new Color(54, 45, 34, 200);
    private static final Color TILE_NAME_COLOR = Color.WHITE;
    private static final Color TILE_DESCRIPTION_COLOR = new Color(255, 255, 255, 235);

    private final DoodleButton closeButton;
    private final Font titleFont;
    private final Font textFont;
    private final Font tileNameLargeFont;
    private final Font tileNameSmallFont;
    private final Font tileDescriptionFont;
    private final Font resultDescriptionFont;

    private Color getRarityColor(Rarity rarity) {
        switch (rarity) {
            case COMMUN:
                return new Color(88, 143, 208);
            case RARE:
                return new Color(92, 174, 90);
            case LEGENDAIRE:
                return new Color(221, 145, 55);
            default:
                return new Color(88, 143, 208);
        }
    }

    private enum DisplayMode {
        BOUTIQUE,
        CLASSEUR
    }

    private enum Rarity {
        COMMUN,
        RARE,
        LEGENDAIRE
    }

    private static final class SkinItem {
        private final String name;
        private final String description;
        private final BufferedImage image;
        private final Rarity rarity;

        private SkinItem(String name, String description, BufferedImage image, Rarity rarity) {
            this.name = name;
            this.description = description;
            this.image = image;
            this.rarity = rarity;
        }
    }

    private final List<SkinItem> allSkins = new ArrayList<>();
    private final List<SkinItem> communList = new ArrayList<>(); // liste des skins communs dans le tirage
    private final List<SkinItem> rareList = new ArrayList<>(); // liste des skins rares dans le tirage
    private final List<SkinItem> legendaireList = new ArrayList<>(); // liste des skins legendaires dans le tirage
    private final Random random = new Random();

    private int currentPack;
    private SkinItem winSkin;
    private DisplayMode mode = DisplayMode.BOUTIQUE;

    public CaseOpeningOverlayPanel() {
        setOpaque(false);
        setLayout(null);
        setVisible(false);

        titleFont = loadFont(48, Font.BOLD);
        textFont = loadFont(26, Font.BOLD);
        tileNameLargeFont = loadFont(24, Font.BOLD);
        tileNameSmallFont = loadFont(16, Font.BOLD);
        tileDescriptionFont = loadFont(12, Font.PLAIN);
        resultDescriptionFont = textFont.deriveFont(22f);

        closeButton = new DoodleButton("Fermer", ButtonColor.RED, 180, 58);
        closeButton.setBgImage("/Button/button_red.png");
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
            public void mouseClicked(MouseEvent e) {
                closeButton.onMouseExit();
                setVisible(false);
                repaint();
            }
        });
        add(closeButton);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutCloseButton();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                layoutCloseButton();
            }
        });

        initSkins();
    }

    public void startOpening(int packNumber) {
        if (allSkins.isEmpty()) {
            return;
        }

        mode = DisplayMode.BOUTIQUE;
        winSkin = rollWinSkin(packNumber);

        closeButton.setEnabled(true);
        closeButton.onMouseExit();
        layoutCloseButton();
        setVisible(true);
        repaint();
    }

    public void openClasseur() {
        mode = DisplayMode.CLASSEUR;
        setVisible(true);
        closeButton.setEnabled(true);
        closeButton.onMouseExit();
        layoutCloseButton();
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
        drawBody(g2, card);

        g2.dispose();
    }

    private void drawBody(Graphics2D g2, Rectangle card) {
        if (mode == DisplayMode.CLASSEUR) {
            drawClasseur(g2, card);
            return;
        }
        drawResult(g2, card);
    }

    private void drawResult(Graphics2D g2, Rectangle card) {
        if (winSkin == null) {
            return;
        }

        int tileX = card.x + (card.width - RESULT_TILE_WIDTH) / 2;
        int tileY = card.y + 96;
        drawSkinBox(g2, winSkin, tileX, tileY, RESULT_TILE_WIDTH, RESULT_TILE_HEIGHT, true);

        g2.setColor(RESULT_TEXT_COLOR);
        g2.setFont(resultDescriptionFont);
        int textY = tileY + RESULT_TILE_HEIGHT + 36;
        drawCenteredText(g2, winSkin.description, card.x + 40, card.width - 80, textY);
    }

    private void drawClasseur(Graphics2D g2, Rectangle card) {
        int columns = 3;
        int tileWidth = 220;
        int tileHeight = 250;
        int gapX = 24;
        int gapY = 20;

        int maxColumns = Math.max(1, Math.min(columns, allSkins.size()));
        int gridWidth = maxColumns * tileWidth + (maxColumns - 1) * gapX;
        int startX = card.x + (card.width - gridWidth) / 2;
        int startY = card.y + 92;

        for (int i = 0; i < allSkins.size(); i++) {
            int row = i / columns;
            int col = i % columns;
            int x = startX + col * (tileWidth + gapX);
            int y = startY + row * (tileHeight + gapY);
            drawSkinBox(g2, allSkins.get(i), x, y, tileWidth, tileHeight, false);
        }
    }

    // dessine un skin dans le classeur ou dans l'ouverture
    private void drawSkinBox(Graphics2D g2, SkinItem item, int x, int y, int width, int height, boolean showDescription) {
        int imageMaxH = showDescription ? 180 : 150;

        drawTileBackground(g2, item, x, y, width, height);

        Rectangle imageBounds = computeImageBounds(item.image, x, y, width, imageMaxH);
        g2.drawImage(item.image, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height, null);

        int textY = y + imageMaxH + 34;
        drawTileName(g2, item.name, x, width, textY, showDescription);

        if (!showDescription) {
            drawTileDescription(g2, item.description, x, width, textY + 22);
        }
    }

    private void drawTileBackground(Graphics2D g2, SkinItem item, int x, int y, int width, int height) {
        g2.setColor(getRarityColor(item.rarity));
        g2.fillRoundRect(x, y, width, height, TILE_ARC, TILE_ARC);
        g2.setColor(TILE_BORDER_COLOR);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(x, y, width - 1, height - 1, TILE_ARC, TILE_ARC);
    }

    private Rectangle computeImageBounds(BufferedImage image, int x, int y, int width, int imageMaxH) {
        int imageMaxW = width - 20;
        int drawW = imageMaxW;
        int drawH = (int) Math.round(drawW * (image.getHeight() / (double) image.getWidth()));
        if (drawH > imageMaxH) {
            drawH = imageMaxH;
            drawW = (int) Math.round(drawH * (image.getWidth() / (double) image.getHeight()));
        }

        int imageX = x + (width - drawW) / 2;
        int imageY = y + 8 + (imageMaxH - drawH) / 2;
        return new Rectangle(imageX, imageY, drawW, drawH);
    }

    private void drawTileName(Graphics2D g2, String name, int x, int width, int baselineY, boolean showDescription) {
        g2.setFont(showDescription ? tileNameLargeFont : tileNameSmallFont);
        g2.setColor(TILE_NAME_COLOR);
        FontMetrics metrics = g2.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(name)) / 2;
        g2.drawString(name, textX, baselineY);
    }

    private void drawTileDescription(Graphics2D g2, String description, int x, int width, int baselineY) {
        g2.setFont(tileDescriptionFont);
        g2.setColor(TILE_DESCRIPTION_COLOR);
        drawCenteredText(g2, description, x + 12, width - 24, baselineY);
    }

    private void drawCenteredText(Graphics2D g2, String text, int x, int width, int baselineY) {
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        if (textWidth <= width) {
            g2.drawString(text, x + (width - textWidth) / 2, baselineY);
            return;
        }

        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int y = baselineY;
        int lineHeight = fm.getHeight();

        for (String word : words) {
            String next = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(next) <= width) {
                line = new StringBuilder(next);
                continue;
            }

            int drawX = x + (width - fm.stringWidth(line.toString())) / 2;
            g2.drawString(line.toString(), drawX, y);
            y += lineHeight;
            line = new StringBuilder(word);
        }

        if (line.length() > 0) {
            int drawX = x + (width - fm.stringWidth(line.toString())) / 2;
            g2.drawString(line.toString(), drawX, y);
        }
    }

    private void drawOverlayBackground(Graphics2D g2) {
        g2.setColor(OVERLAY_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawCard(Graphics2D g2, Rectangle card) {
        g2.setColor(CARD_COLOR);
        g2.fillRoundRect(card.x, card.y, card.width, card.height, CARD_ARC, CARD_ARC);
        g2.setColor(CARD_BORDER_COLOR);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(card.x, card.y, card.width - 1, card.height - 1, CARD_ARC, CARD_ARC);
    }

    private void drawTitle(Graphics2D g2, Rectangle card) {
        g2.setColor(TITLE_COLOR);
        g2.setFont(titleFont);
        String title = mode == DisplayMode.CLASSEUR ? "Classeur" : "Pack " + nameCurrentPack() + " ouvert";
        g2.drawString(title, card.x + 28, card.y + 54);
    }

    private String nameCurrentPack() {
        switch (currentPack) {
            case 1:
                return "Miteux";
            case 2:
                return "Chanceux";
            case 3:
                return "LUXUEUX";
            default:
                return "???";
        }
    }

    // le bouton de fermeture est reuse a plusieurs overlay
    private void layoutCloseButton() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        Rectangle card = getCardBounds();
        Dimension buttonSize = closeButton.getPreferredSize();
        closeButton.setBounds(
            card.x + card.width - buttonSize.width - 18,
            card.y + 14,
            buttonSize.width,
            buttonSize.height
        );
    }

    private SkinItem rollWinSkin(int packNumber) {
        int[] chances = PACK_CHANCES[packNumber - 1];
        int roll = random.nextInt(100);

        if (roll < chances[0]) {
            return communList.get(random.nextInt(communList.size()));
        }
        if (roll < chances[0] + chances[1]) {
            return rareList.get(random.nextInt(rareList.size()));
        }
        return legendaireList.get(random.nextInt(legendaireList.size()));
    }

    private Rectangle getCardBounds() {
        int baseW = mode == DisplayMode.CLASSEUR ? 980 : 760;
        int baseH = mode == DisplayMode.CLASSEUR ? 620 : 500;

        int cardW = Math.min(getWidth() - 120, baseW);
        int cardH = Math.min(getHeight() - 100, baseH);

        cardW = Math.max(cardW, mode == DisplayMode.CLASSEUR ? 820 : 640);
        cardH = Math.max(cardH, mode == DisplayMode.CLASSEUR ? 460 : 420);

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

    private void initSkins() { // on met les skins qu'on veut dans le tirage ici, chacun dans sa liste de rarete
        addSkin("NUL", "Basique et sans effet special.", "/boutique/nul.png", Rarity.COMMUN);
        addSkin("BOF", "Un skin correct, mais encore commun.", "/boutique/bof.png", Rarity.COMMUN);
        addSkin("BIEN", "Un style propre qui sort du lot.", "/boutique/bien.png", Rarity.RARE);
        addSkin("GOLDGOLDGOLD", "Jackpot legendaire. Tres difficile a obtenir.", "/boutique/GOLDGOLDGOLD.png", Rarity.LEGENDAIRE);
    }

    private void addSkin(String name, String description, String path, Rarity rarity) {
        BufferedImage image = ImageCache.get(path);
        if (image == null) {
            return;
        }

        SkinItem item = new SkinItem(name, description, image, rarity);
        allSkins.add(item);
        switch (rarity) {
            case COMMUN:
                communList.add(item);
                break;
            case RARE:
                rareList.add(item);
                break;
            case LEGENDAIRE:
                legendaireList.add(item);
                break;
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
}
