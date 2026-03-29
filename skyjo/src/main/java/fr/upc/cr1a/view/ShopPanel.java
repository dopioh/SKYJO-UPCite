package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.utils.ImageCache;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

/**
 * Panel boutique avec affichage des packs et informations de probabilites.
 */
public class ShopPanel extends JPanel {

    private static final int PACK_HEIGHT = 320;
    private static int money = 1000;
    
    private final DoodleButton backBtn;
    private final DoodleButton moneyBtn;
    private final DoodleButton classeurBtn;
    private final DoodleButton[] buyButtons = new DoodleButton[3];
    private final JLabel[] packImages = new JLabel[3];
    private final BufferedImage[] packSources;

    private final JPanel topBar;
    private final JPanel chanceContainer;
    private final JLabel chanceLabel;
    private final JPanel cardsRow;
    private final CaseOpeningOverlayPanel caseOpeningOverlay;
    private final Image backgroundImage;

    public ShopPanel() {
        setLayout(new BorderLayout());

        backgroundImage = ImageCache.get("/background.png");
        packSources = new BufferedImage[] {
            ImageCache.get("/boutique/nul.png"),
            ImageCache.get("/boutique/bof.png"),
            ImageCache.get("/boutique/bien.png")
        };

        topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        JPanel leftColumn = new JPanel();
        leftColumn.setOpaque(false);
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));

        backBtn = new DoodleButton("Retour", ButtonColor.RED, 200, 60);
        backBtn.setBgImage("/Button/button_red.png");
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftColumn.add(backBtn);
        leftColumn.add(Box.createVerticalStrut(8));

        moneyBtn = new DoodleButton("", ButtonColor.RED, 200, 60);
        moneyBtn.setBgImage("/Button/button_yellow.png");
        moneyBtn.setCursor(Cursor.getDefaultCursor());
        moneyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshMoneyButton();
        leftColumn.add(moneyBtn);

        leftPanel.add(leftColumn);

        JPanel centerTopPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerTopPanel.setOpaque(false);
        classeurBtn = new DoodleButton("Classeur", ButtonColor.YELLOW, 200, 60);
        classeurBtn.setBgImage("/Button/button_yellow.png");
        centerTopPanel.add(classeurBtn);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        chanceContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(255, 255, 255, 180));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chanceContainer.setOpaque(false);

        chanceLabel = new JLabel(
            "<html>1: 80%C 15%R 5%L<br/>2: 65%C 25%R 10%L<br/>3: 55%C 30%R 15%L</html>" // 🙂🔫
        );
        chanceLabel.setForeground(Color.WHITE);
        chanceLabel.setHorizontalAlignment(SwingConstants.LEFT);
        chanceContainer.add(chanceLabel, BorderLayout.CENTER);

        rightPanel.add(chanceContainer);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(centerTopPanel, BorderLayout.CENTER);
        topBar.add(rightPanel, BorderLayout.EAST);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        cardsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 38, 18));
        cardsRow.setOpaque(false);

        for (int i = 0; i < 3; i++) {
            JPanel column = new JPanel();
            column.setOpaque(false);
            column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

            packImages[i] = new JLabel();
            packImages[i].setHorizontalAlignment(SwingConstants.CENTER);
            packImages[i].setAlignmentX(Component.CENTER_ALIGNMENT);

            BufferedImage source = packSources[i];
            if (source != null) {
                int packWidth = (int) Math.round(PACK_HEIGHT * (source.getWidth() / (double) source.getHeight()));
                Image scaled = source.getScaledInstance(packWidth, PACK_HEIGHT, Image.SCALE_SMOOTH);
                packImages[i].setIcon(new ImageIcon(scaled));
                packImages[i].setPreferredSize(new Dimension(packWidth, PACK_HEIGHT));
            }

            if (i == 0) {
                buyButtons[i] = new DoodleButton("100$", ButtonColor.GREEN, 220, 70, "/Button/Shop.png");
            } else if (i == 1) {
                buyButtons[i] = new DoodleButton("200$", ButtonColor.GREEN, 220, 70, "/Button/Shop.png");
            } else {
                buyButtons[i] = new DoodleButton("300$", ButtonColor.GREEN, 220, 70, "/Button/Shop.png");
            }

            buyButtons[i].setBgImage("/Button/button_green.png");
            buyButtons[i].setAlignmentX(Component.CENTER_ALIGNMENT);

            column.add(packImages[i]);
            column.add(Box.createVerticalStrut(16));
            column.add(buyButtons[i]);
            cardsRow.add(column);
        }
        

        topBar.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        chanceLabel.setFont(new Font("Arial", Font.BOLD, 15));
        chanceContainer.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        center.add(cardsRow);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(topBar, BorderLayout.NORTH);
        contentPanel.add(center, BorderLayout.CENTER);

        JLayeredPane overlayStack = new JLayeredPane();
        overlayStack.setOpaque(false);

        caseOpeningOverlay = new CaseOpeningOverlayPanel();
        overlayStack.add(contentPanel, Integer.valueOf(JLayeredPane.DEFAULT_LAYER));
        overlayStack.add(caseOpeningOverlay, Integer.valueOf(JLayeredPane.MODAL_LAYER));

        overlayStack.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                fillOverlayStack(overlayStack);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                fillOverlayStack(overlayStack);
            }
        });
        fillOverlayStack(overlayStack);

        add(overlayStack, BorderLayout.CENTER);
    }

    private void fillOverlayStack(JLayeredPane overlayStack) {
        int width = overlayStack.getWidth();
        int height = overlayStack.getHeight();
        for (Component component : overlayStack.getComponents()) {
            component.setBounds(0, 0, width, height);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public DoodleButton getBackButton() {
        return backBtn;
    }

    public DoodleButton[] getBuyButtons() {
        return buyButtons;
    }

    public DoodleButton getClasseurButton() {
        return classeurBtn;
    }

    public void startCaseOpening(int packNumber) {
        clearHoverStates();
        caseOpeningOverlay.startOpening(packNumber);
    }

    public void openClasseur() {
        clearHoverStates();
        caseOpeningOverlay.openClasseur();
    }

    public boolean isCaseOpeningVisible() {
        return caseOpeningOverlay.isOpen();
    }

    private void clearHoverStates() {
        backBtn.onMouseExit();
        classeurBtn.onMouseExit();
        for (DoodleButton buyButton : buyButtons) {
            buyButton.onMouseExit();
        }
    }

    // TEMPORAIRE
    public void setMoney(int amount) {
        if (amount < 0) {
            money = 0;
        } else {
            money = amount;
        }
        refreshMoneyButton();
    }

    public int getMoney() {
        return money;
    }

    public void refreshMoneyButton() {
        moneyBtn.setText(money + " $");
    }
}
