package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.utils.ImageCache;

import javax.swing.*;
import java.awt.*;

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
    private Image backgroundImage;

    public MainMenuPanel() {
        setLayout(new BorderLayout());

        //fond
        backgroundImage = ImageCache.get("/background.png");

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        topBar.setOpaque(false);
        Image scaledAccount = ImageCache.get("/Account.png").getScaledInstance(48, 48, Image.SCALE_SMOOTH);
        accountBtn = new JButton(new ImageIcon(scaledAccount));
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
        shopBtn.setBgImage("/Button/button_red.png");
        playBtn.setBgImage("/Button/button_yellow.png");
        settingsBtn.setBgImage("/Button/button_green.png");

        panel.add(shopBtn);
        panel.add(playBtn);
        panel.add(settingsBtn);

        gbc.gridy = 1;
        centre.add(panel, gbc);

        add(centre, BorderLayout.CENTER);
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

    public DoodleButton getShopButton() { return shopBtn; }
    public DoodleButton getPlayButton() { return playBtn; }
    public DoodleButton getSettingsButton() { return settingsBtn; }
    public JButton getAccountButton() { return accountBtn; }
}
