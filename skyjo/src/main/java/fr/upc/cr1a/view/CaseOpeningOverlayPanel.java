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

    private static ArrayList<String> unlockedSkinPaths = new ArrayList<>();
    private static String selectedSkinPath = "/cartes/carteDos.png";

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
    private static final int CLASSEUR_TOP_PADDING = 92;

    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 140);
    private static final Color CARD_COLOR = new Color(248, 243, 226, 255);
    private static final Color CARD_BORDER_COLOR = new Color(54, 45, 34, 215);
    private static final Color TITLE_COLOR = new Color(46, 38, 26);
    private static final Color RESULT_TEXT_COLOR = new Color(50, 40, 28);
    private static final Color TILE_UNLOCKED_COLOR = new Color(58, 150, 83, 230);
    private static final Color EQUIP_BUTTON_COLOR = new Color(43, 104, 62, 235);
    private static final Color EQUIP_BUTTON_SELECTED_COLOR = new Color(218, 113, 72, 240);
    private static final Color EQUIP_BUTTON_BORDER_COLOR = new Color(18, 47, 29, 230);
    private static final Color TILE_NAME_COLOR = Color.WHITE;

    private static final int EQUIP_BUTTON_HEIGHT = 22;
    private static final int EQUIP_BUTTON_MARGIN_X = 8;
    private static final int EQUIP_BUTTON_BOTTOM_PADDING = 8;

    private final DoodleButton closeButton;
    private final Font titleFont;
    private final Font textFont;
    private final Font tileNameLargeFont;
    private final Font tileNameSmallFont;
    private final Font resultDescriptionFont;

    private Color getRarityColor(Rarity rarity) {
        switch (rarity) {
            case COMMUN:
                return new Color(46, 38, 26);
            case RARE:
                return new Color(46, 38, 26);
            case LEGENDAIRE:
                return new Color(46, 38, 26);
            default:
                return new Color(46, 38, 26);
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
        private final String path;
        private final BufferedImage image;
        private final Rarity rarity;

        private SkinItem(String name, String description, String path, BufferedImage image, Rarity rarity) {
            this.name = name;
            this.description = description;
            this.path = path;
            this.image = image;
            this.rarity = rarity;
        }
    }

    private List<SkinItem> allSkins = new ArrayList<>();
    private List<SkinItem> communList = new ArrayList<>(); // liste des skins communs dans le tirage
    private List<SkinItem> rareList = new ArrayList<>(); // liste des skins rares dans le tirage
    private List<SkinItem> legendaireList = new ArrayList<>(); // liste des skins legendaires dans le tirage
    private final Random random = new Random();

    private int currentPack;
    private SkinItem winSkin;
    private DisplayMode mode = DisplayMode.BOUTIQUE;

    public CaseOpeningOverlayPanel() {
        unlockSkin("/cartes/carteDos.png");

        setOpaque(false);
        setLayout(null);
        setVisible(false);

        titleFont = loadFont(48, Font.BOLD);
        textFont = loadFont(26, Font.BOLD);
        tileNameLargeFont = loadFont(24, Font.BOLD);
        tileNameSmallFont = loadFont(16, Font.BOLD);
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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClasseurClick(e.getPoint());
            }
        });

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
        currentPack = packNumber;
        winSkin = rollWinSkin(currentPack);
        if (winSkin != null) {
            unlockSkin(winSkin.path);
        }

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
        if (allSkins.isEmpty()) {
            return;
        }

        int count = allSkins.size();
        int outerPadding = 22;
        int gap = 10;

        int contentLeft = card.x + outerPadding;
        int contentWidth = Math.max(1, card.width - outerPadding * 2);
        int contentTop = card.y + CLASSEUR_TOP_PADDING;
        int contentHeight = Math.max(1, card.height - CLASSEUR_TOP_PADDING - 22);

        int maxAllowedWidth = 140;
        int minAllowedWidth = 72;
        int tileWidth = (contentWidth - (count - 1) * gap) / count;
        tileWidth = Math.max(minAllowedWidth, Math.min(maxAllowedWidth, tileWidth));
        int tileHeight = Math.max(100, (int) Math.round(tileWidth * 1.35));

        int totalRowWidth = count * tileWidth + (count - 1) * gap;
        int startX = contentLeft + (contentWidth - totalRowWidth) / 2;
        int startY = contentTop + (contentHeight - tileHeight) / 2;

        for (int i = 0; i < count; i++) {
            int x = startX + i * (tileWidth + gap);
            drawSkinBox(g2, allSkins.get(i), x, startY, tileWidth, tileHeight, false);
        }
    }

    // dessine un skin dans le classeur ou dans l'ouverture
    private void drawSkinBox(Graphics2D g2, SkinItem item, int x, int y, int width, int height, boolean showDescription) {
        boolean inClasseur = !showDescription;
        boolean isUnlocked = isSkinUnlocked(item.path);
        boolean isSelected = item.path.equals(getSelectedSkinPath());
        drawTileBackground(g2, item, x, y, width, height, inClasseur && isUnlocked, inClasseur && isSelected);

        if (showDescription) { // dans le classeur on affiche pas la description
            g2.setFont(tileNameLargeFont);
            FontMetrics nameMetrics = g2.getFontMetrics();

            int horizontalPadding = 10;
            int verticalPadding = 18;
            int nameGap = 10;

            int imageAreaW = Math.max(1, width - horizontalPadding * 2);
            int imageAreaH = Math.max(40, height - verticalPadding * 2 - nameGap - nameMetrics.getHeight());

            Rectangle scaledImage = computeImageBounds(item.image, 0, 0, imageAreaW, imageAreaH);

            int blockHeight = scaledImage.height + nameGap + nameMetrics.getHeight();
            int blockTop = y + (height - blockHeight) / 2;

            int imageX = x + (width - scaledImage.width) / 2;
            int imageY = blockTop;
            g2.drawImage(item.image, imageX, imageY, scaledImage.width, scaledImage.height, null);

            int titleBaseline = imageY + scaledImage.height + nameGap + nameMetrics.getAscent();
            drawTileName(g2, item.name, x, width, titleBaseline, true);
            return;
        }

        int imageTopPadding = 8;
        int imageBottomMargin = isUnlocked ? 62 : 34;
        int imageAvailableH = Math.max(30, height - imageTopPadding - imageBottomMargin);
        Rectangle imageBounds = computeImageBounds(item.image, x + 8, y + imageTopPadding, width - 16, imageAvailableH);
        g2.drawImage(item.image, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height, null);

        int textY = isUnlocked ? y + height - EQUIP_BUTTON_HEIGHT - EQUIP_BUTTON_BOTTOM_PADDING - 8 : y + height - 10;
        drawTileName(g2, item.name, x, width, textY, false);

        if (isUnlocked) {
            Rectangle buttonBounds = getEquipButtonBounds(x, y, width, height);
            drawEquipButton(g2, buttonBounds, isSelected);
        }
    }

    private void drawTileBackground(Graphics2D g2, SkinItem item, int x, int y, int width, int height, boolean unlockedInClasseur, boolean selectedInClasseur) {
        g2.setColor(unlockedInClasseur ? TILE_UNLOCKED_COLOR : getRarityColor(item.rarity));
        g2.fillRoundRect(x, y, width, height, TILE_ARC, TILE_ARC);
    }

    private void handleClasseurClick(Point clickPoint) {
        if (!isVisible() || mode != DisplayMode.CLASSEUR) {
            return;
        }

        Rectangle card = getCardBounds();
        int count = allSkins.size();
        int outerPadding = 22;
        int gap = 10;

        int contentLeft = card.x + outerPadding;
        int contentWidth = Math.max(1, card.width - outerPadding * 2);
        int contentTop = card.y + CLASSEUR_TOP_PADDING;
        int contentHeight = Math.max(1, card.height - CLASSEUR_TOP_PADDING - 22);

        int maxAllowedWidth = 140;
        int minAllowedWidth = 72;
        int tileWidth = (contentWidth - (count - 1) * gap) / count;
        tileWidth = Math.max(minAllowedWidth, Math.min(maxAllowedWidth, tileWidth));
        int tileHeight = Math.max(100, (int) Math.round(tileWidth * 1.35));

        int totalRowWidth = count * tileWidth + (count - 1) * gap;
        int startX = contentLeft + (contentWidth - totalRowWidth) / 2;
        int startY = contentTop + (contentHeight - tileHeight) / 2;

        for (int i = 0; i < count; i++) {
            int x = startX + i * (tileWidth + gap);
            Rectangle bounds = new Rectangle(x, startY, tileWidth, tileHeight);
            SkinItem clickedSkin = allSkins.get(i);
            if (!isSkinUnlocked(clickedSkin.path)) {
                continue;
            }

            Rectangle buttonBounds = getEquipButtonBounds(bounds.x, bounds.y, bounds.width, bounds.height);
            if (buttonBounds.contains(clickPoint)) {
                setSelectedSkinPath(clickedSkin.path);
                repaint();
                return;
            }
        }
    }

    private Rectangle getEquipButtonBounds(int x, int y, int width, int height) {
        int buttonWidth = Math.max(24, width - EQUIP_BUTTON_MARGIN_X * 2);
        int buttonX = x + (width - buttonWidth) / 2;
        int buttonY = y + height - EQUIP_BUTTON_BOTTOM_PADDING - EQUIP_BUTTON_HEIGHT;
        return new Rectangle(buttonX, buttonY, buttonWidth, EQUIP_BUTTON_HEIGHT);
    }

    private void drawEquipButton(Graphics2D g2, Rectangle bounds, boolean isSelected) {
        g2.setColor(isSelected ? EQUIP_BUTTON_SELECTED_COLOR : EQUIP_BUTTON_COLOR);
        g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
        g2.setColor(EQUIP_BUTTON_BORDER_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, 12, 12);

        String label = isSelected ? "Equipe" : "Equiper";
        g2.setFont(tileNameSmallFont);
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int tx = bounds.x + (bounds.width - fm.stringWidth(label)) / 2;
        int ty = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(label, tx, ty);
    }

    private Rectangle computeImageBounds(BufferedImage image, int x, int y, int width, int height) {
        int imageMaxW = Math.max(1, width);
        int imageMaxH = Math.max(1, height);
        int drawW = imageMaxW;
        int drawH = (int) Math.round(drawW * (image.getHeight() / (double) image.getWidth()));
        if (drawH > imageMaxH) {
            drawH = imageMaxH;
            drawW = (int) Math.round(drawH * (image.getWidth() / (double) image.getHeight()));
        }

        int imageX = x + (width - drawW) / 2;
        int imageY = y + (height - drawH) / 2;
        return new Rectangle(imageX, imageY, drawW, drawH);
    }

    private void drawTileName(Graphics2D g2, String name, int x, int width, int baselineY, boolean showDescription) {
        g2.setFont(showDescription ? tileNameLargeFont : tileNameSmallFont);
        g2.setColor(TILE_NAME_COLOR);
        FontMetrics metrics = g2.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(name)) / 2;
        g2.drawString(name, textX, baselineY);
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
            SkinItem item = communList.get(random.nextInt(communList.size()));
            if (item.name.equals("OG")) {
                return rollWinSkin(packNumber);
            }
            return item;
        }
        if (roll < chances[0] + chances[1]) {
            SkinItem item = rareList.get(random.nextInt(rareList.size()));
            if (item.name.equals("OG")) {
                return rollWinSkin(packNumber);
            }
            return item;
        }
        else {
            SkinItem item = legendaireList.get(random.nextInt(legendaireList.size()));
            if (item.name.equals("OG")) {
                return rollWinSkin(packNumber);
            }
            return item;
        }
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
        addSkin("HEUREUX", "On pense qu'il est heureux.", "/boutique/skins/heureux.png", Rarity.LEGENDAIRE);
        addSkin("CLOWN", "haha.", "/boutique/skins/clown.png", Rarity.LEGENDAIRE);
        addSkin("POKEMON", "Tous droits réservés à The Pokémon Company (on a pas les droits)", "/boutique/skins/pokemon.png", Rarity.RARE);
        addSkin("YUGIOH", "Tous droits réservés à Konami (on a pas les droits)", "/boutique/skins/yugioh.png", Rarity.RARE);
        addSkin("SKYJOBLEU", "Un skin basique, pas rare du tout.", "/boutique/skins/skyjobleu.png", Rarity.COMMUN);
        addSkin("SKYOJOYELLOW", "Un skin simple, pas rare du tout.", "/boutique/skins/skyjoyellow.png", Rarity.COMMUN);
        addSkin("SKYJOPINK", "Un skin basique, pas rare du tout.", "/boutique/skins/skyjopink.png", Rarity.COMMUN);
        addSkin("SKYJOVERT", "Un skin simple, pas rare du tout.", "/boutique/skins/skyjovert.png", Rarity.COMMUN);
        addSkin("SOLEIL", "Il fait chaud maintenant...", "/boutique/skins/sun.png", Rarity.RARE);
        addSkin("OG", "Le tout premier skin", "/cartes/carteDos.png", Rarity.COMMUN);

    }

    private void addSkin(String name, String description, String path, Rarity rarity) {
        BufferedImage image = ImageCache.get(path);
        if (image == null) {
            return;
        }

        SkinItem item = new SkinItem(name, description, path, image, rarity);
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

    // TEMPORAIRE 
    public static ArrayList<String> getUnlockedSkinPaths() {
        return new ArrayList<>(unlockedSkinPaths);
    }

    public static boolean unlockSkin(String skinPath) {
        if (skinPath == null) {
            return false;
        }
        if (unlockedSkinPaths.contains(skinPath)) {
            return false;
        }
        unlockedSkinPaths.add(skinPath);
        return true;
    }

    public static boolean isSkinUnlocked(String skinPath) {
        if (skinPath == null) {
            return false;
        }
        return unlockedSkinPaths.contains(skinPath);
    }

    public static String getSelectedSkinPath() {
        if (selectedSkinPath == null) {
            selectedSkinPath = "/cartes/carteDos.png";
        }
        return selectedSkinPath;
    }

    public static void setSelectedSkinPath(String skinPath) {
        if (skinPath == null) {
            return;
        }
        selectedSkinPath = skinPath;
        unlockSkin(skinPath);
    }
}
