package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.utils.ImageCache;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Panel du main menu avec les bouttons
 * 
 * @author Ian, Nassim
 */
public class MainMenuPanel extends JPanel {

    private DoodleButton shopBtn;
    private DoodleButton playBtn;
    private DoodleButton settingsBtn;
    private JButton accountBtn;
    private JButton podiumBtn;
    private AccountStatsOverlayPanel accountStatsOverlay;
    private Image backgroundImage;
    private Image charactersImage;
    private BufferedImage cachedBackground;

    public MainMenuPanel(Frame frame) {
        setLayout(new BorderLayout());

        //fond
        backgroundImage = ImageCache.get("/background.png");
        charactersImage = ImageCache.get("/main_menu_characters.png");

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 16));
        topBar.setOpaque(false);
        Image scaledPodium = ImageCache.get("/podium.png").getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        podiumBtn = new JButton(new ImageIcon(scaledPodium));
        podiumBtn.setBorderPainted(false);
        podiumBtn.setContentAreaFilled(false);
        podiumBtn.setFocusPainted(false);
        podiumBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topBar.add(podiumBtn);
        Image scaledAccount = ImageCache.get("/Account.png").getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        accountBtn = new JButton(new ImageIcon(scaledAccount));
        accountBtn.setBorderPainted(false);
        accountBtn.setContentAreaFilled(false);
        accountBtn.setFocusPainted(false);
        accountBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topBar.add(accountBtn);

        // contenu centré
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 20, 10, 20);

        // Logo
        Image scaledLogo = ImageCache.get("/skyjo_logo.png").getScaledInstance(400, -1, Image.SCALE_SMOOTH);
        JLabel titre = new JLabel(new ImageIcon(scaledLogo));
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        centre.add(titre, gbc);

        // boutons
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setOpaque(false);

        shopBtn = new DoodleButton("Boutique", ButtonColor.RED, 300, 100, "/Button/Shop.png");
        playBtn = new DoodleButton("Jouer", ButtonColor.YELLOW, 300, 100, "/Button/Play.png");
        settingsBtn = new DoodleButton("Réglages", ButtonColor.GREEN, 300, 100, "/Button/Settings.png");

        panel.add(shopBtn);
        panel.add(playBtn);
        panel.add(settingsBtn);

        gbc.gridy = 1;
        centre.add(panel, gbc);

        JLayeredPane overlayStack = new JLayeredPane() {
            @Override
            public void doLayout() {
                fillOverlayStack(this, topBar, centre);
            }
        };
        overlayStack.setOpaque(false);

        accountStatsOverlay = new AccountStatsOverlayPanel(frame);
        overlayStack.add(centre, Integer.valueOf(JLayeredPane.DEFAULT_LAYER));
        overlayStack.add(topBar, Integer.valueOf(JLayeredPane.DEFAULT_LAYER));
        overlayStack.add(accountStatsOverlay, Integer.valueOf(JLayeredPane.MODAL_LAYER));

        add(overlayStack, BorderLayout.CENTER);
    }

    private void fillOverlayStack(JLayeredPane overlayStack, JPanel topBar, JPanel centre) {
        int width = overlayStack.getWidth();
        int height = overlayStack.getHeight();
        int topHeight = topBar.getPreferredSize().height;
        int charH = height * 30 / 100;

        topBar.setBounds(0, 0, width, topHeight);
        centre.setBounds(0, topHeight, width, Math.max(0, height - topHeight - charH / 2));
        accountStatsOverlay.setBounds(0, 0, width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth(), h = getHeight();
        if (backgroundImage != null) {
            if (cachedBackground == null || cachedBackground.getWidth() != w || cachedBackground.getHeight() != h) {
                cachedBackground = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D bg = cachedBackground.createGraphics();
                bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                bg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                bg.drawImage(backgroundImage, 0, 0, w, h, null);
                bg.dispose();
            }
            g.drawImage(cachedBackground, 0, 0, null);
        }
        if (charactersImage != null) {
            int imgW = charactersImage.getWidth(null);
            int imgH = charactersImage.getHeight(null);
            if (imgW > 0 && imgH > 0) {
                int targetH = h * 30 / 100;
                int targetW = imgW * targetH / imgH;
                g.drawImage(charactersImage, (w - targetW) / 2, h - targetH, targetW, targetH, null);
            }
        }
    }

    public DoodleButton getShopButton() { return shopBtn; }
    public DoodleButton getPlayButton() { return playBtn; }
    public DoodleButton getSettingsButton() { return settingsBtn; }
    public JButton getAccountButton() { return accountBtn; }
    public JButton getPodiumButton() { return podiumBtn; }

    public void openAccountStats() {
        clearHoverStates();
        accountStatsOverlay.openStats();
    }

    public boolean isAccountStatsVisible() {
        return accountStatsOverlay.isOpen();
    }

    private void clearHoverStates() {
        shopBtn.onMouseExit();
        playBtn.onMouseExit();
        settingsBtn.onMouseExit();
    }
}
