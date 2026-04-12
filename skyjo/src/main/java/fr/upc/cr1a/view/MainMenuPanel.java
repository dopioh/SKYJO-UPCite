package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.utils.ImageCache;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Panel du main menu avec les bouttons
 * 
 * @author Ian
 */
public class MainMenuPanel extends JPanel {

    private DoodleButton shopBtn;
    private DoodleButton playBtn;
    private DoodleButton settingsBtn;
    private JButton accountBtn;
    private JButton podiumBtn;
    private Image backgroundImage;
    private BufferedImage cachedBackground;

    public MainMenuPanel() {
        setLayout(new BorderLayout());

        //fond
        backgroundImage = ImageCache.get("/background.png");

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 16));
        topBar.setOpaque(false);
        podiumBtn = new JButton(new ImageIcon(scaleImage("/podium.png", 60, 60)));
        podiumBtn.setBorderPainted(false);
        podiumBtn.setContentAreaFilled(false);
        podiumBtn.setFocusPainted(false);
        podiumBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topBar.add(podiumBtn);
        accountBtn = new JButton(new ImageIcon(scaleImage("/Account.png", 60, 60)));
        accountBtn.setBorderPainted(false);
        accountBtn.setContentAreaFilled(false);
        accountBtn.setFocusPainted(false);
        accountBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topBar.add(accountBtn);
        add(topBar, BorderLayout.NORTH);

        // contenu centré
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(20, 20, 20, 20);

        // Logo
        BufferedImage logoSrc = ImageCache.get("/skyjo_logo.png");
        int logoH = 400 * logoSrc.getHeight() / logoSrc.getWidth();
        JLabel titre = new JLabel(new ImageIcon(scaleImage("/skyjo_logo.png", 400, logoH)));
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

        add(centre, BorderLayout.CENTER);
    }

    // redimensionne avec bicubique pour eviter le pixelisé sur windows
    private BufferedImage scaleImage(String path, int w, int h) {
        BufferedImage src = ImageCache.get(path);
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return result;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            int w = getWidth(), h = getHeight();
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
    }

    public DoodleButton getShopButton() { return shopBtn; }
    public DoodleButton getPlayButton() { return playBtn; }
    public DoodleButton getSettingsButton() { return settingsBtn; }
    public JButton getAccountButton() { return accountBtn; }
    public JButton getPodiumButton() { return podiumBtn; }
}
