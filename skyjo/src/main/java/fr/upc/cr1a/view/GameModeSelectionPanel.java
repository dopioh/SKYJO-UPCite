package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.utils.ImageCache;

import javax.swing.*;
import java.awt.*;

/**
 *Panel de sélection du mode de jeu
 *
 */
public class GameModeSelectionPanel extends JPanel {

    private DoodleButton localBtn;
    private DoodleButton multiBtn;
    private DoodleButton backBtn;
    private Image backgroundImage;

    public GameModeSelectionPanel() {
        setLayout(new BorderLayout());

        backgroundImage = ImageCache.get("/background.png");

        //bouton retour
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        backBtn = new DoodleButton("Retour", ButtonColor.RED, 220, 60, "/Button/retour.png");
        backBtn.setFontSize(32f);
        backBtn.setIconSide(DoodleButton.IconSide.LEFT);
        topBar.add(backBtn);
        add(topBar, BorderLayout.NORTH);

        //contenu centré
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(20, 20, 20, 20);

        //titre
        JLabel titre = new JLabel("Choisissez votre mode de jeu");
        titre.setFont(new Font("Arial", Font.BOLD, 48));
        titre.setForeground(Color.WHITE);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        centre.add(titre, gbc);

        //boutons Local et Multi
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 40));
        buttonsPanel.setOpaque(false);

        localBtn = new DoodleButton("Local", ButtonColor.YELLOW, 350, 150, "/Button/Play.png");
        multiBtn = new DoodleButton("Multiplayer", ButtonColor.GREEN, 350, 150, "/Button/Settings.png"); //a changer

        buttonsPanel.add(localBtn);
        buttonsPanel.add(multiBtn);

        gbc.gridy = 1;
        gbc.gridwidth = 2;
        centre.add(buttonsPanel, gbc);

        //descriptions sous les boutons
        JPanel descriptionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 10));
        descriptionsPanel.setOpaque(false);

        JLabel localDesc = new JLabel("<html><div style='text-align: center; width: 350px;'>" +
                "Jouez en local sur le même ordinateur<br/>avec vos amis ou contre des bots</div></html>");
        localDesc.setFont(new Font("Arial", Font.PLAIN, 16));
        localDesc.setForeground(Color.WHITE);
        localDesc.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel multiDesc = new JLabel("<html><div style='text-align: center; width: 350px;'>" +
                "Rejoignez ou créez un salon<br/>pour jouer en ligne avec vos amis</div></html>");
        multiDesc.setFont(new Font("Arial", Font.PLAIN, 16));
        multiDesc.setForeground(Color.WHITE);
        multiDesc.setHorizontalAlignment(SwingConstants.CENTER);

        descriptionsPanel.add(localDesc);
        descriptionsPanel.add(multiDesc);

        gbc.gridy = 2;
        centre.add(descriptionsPanel, gbc);

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

    public DoodleButton getLocalButton() {
        return localBtn;
    }

    public DoodleButton getMultiButton() {
        return multiBtn;
    }

    public DoodleButton getBackButton() {
        return backBtn;
    }
}
