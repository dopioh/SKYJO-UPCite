package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;

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

    public MainMenuPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        
        // Titre
        JLabel titre = new JLabel("SKYJO");
        titre.setFont(new Font("Arial", Font.BOLD, 60));
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        add(titre, gbc);

        // les boutons horizontal
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setOpaque(false);

        shopBtn = new DoodleButton("Boutique", ButtonColor.RED, 300, 100, "/Button/Shop.png");
        playBtn = new DoodleButton("Jouer", ButtonColor.GREEN, 300, 100, "/Button/Play.png");
        settingsBtn = new DoodleButton("Réglages", ButtonColor.TURQUOISE, 300, 100, "/Button/Settings.png");

        panel.add(shopBtn);
        panel.add(playBtn);
        panel.add(settingsBtn);

        gbc.gridy = 1;
        add(panel, gbc);
    }

    public DoodleButton getShopButton() { return shopBtn; }
    public DoodleButton getPlayButton() { return playBtn; }
    public DoodleButton getSettingsButton() { return settingsBtn; }
}
