package fr.upc.cr1a.view;

import javax.swing.*;
import java.awt.*;

/**
 * Panel du main menu avec les bouttons
 */
public class MainMenuPanel extends JPanel {
    
    private JButton shopButton;
    private JButton playButton;
    private JButton settingsButton;
    
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        shopButton = new JButton("Boutique");
        playButton = new JButton("Jouer");
        settingsButton = new JButton("Réglages");

        //suppression du rectangle bleu bizarre
        shopButton.setFocusPainted(false);
        playButton.setFocusPainted(false);
        settingsButton.setFocusPainted(false);
        
        buttonPanel.add(shopButton);
        buttonPanel.add(playButton);
        buttonPanel.add(settingsButton);
        
        gbc.gridy = 1;
        add(buttonPanel, gbc);
    }
    
    public JButton getShopButton() {
        return shopButton;
    }
    
    public JButton getPlayButton() {
        return playButton;
    }
    
    public JButton getSettingsButton() {
        return settingsButton;
    }
}
