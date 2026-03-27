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

        // police doodle
        Font doodle;
        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            doodle = stream != null ? Font.createFont(Font.TRUETYPE_FONT, stream) : new Font("Arial", Font.PLAIN, 12);
        } catch (Exception e) {
            doodle = new Font("Arial", Font.PLAIN, 12);
        }

        //contenu centré
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 40, 8, 40);
        gbc.anchor = GridBagConstraints.WEST;

        //titre
        JLabel titre = new JLabel("Choisissez votre mode de jeu");
        titre.setFont(doodle.deriveFont(Font.BOLD, 58f));
        titre.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(-60, 40, 8, 40);
        centre.add(titre, gbc);
        gbc.insets = new Insets(8, 40, 8, 40);

        gbc.gridwidth = 1;

        localBtn = new DoodleButton("Local", ButtonColor.YELLOW, 350, 150, "/Button/local.png");
        multiBtn = new DoodleButton("Multiplayer", ButtonColor.GREEN, 350, 150, "/Button/multi.png");

        Font descFont = doodle.deriveFont(Font.BOLD, 26f);

        // local
        JPanel localDesc = new JPanel();
        localDesc.setLayout(new BoxLayout(localDesc, BoxLayout.Y_AXIS));
        localDesc.setOpaque(false);
        JLabel localLine1 = new JLabel("Jouez en local sur le même ordinateur");
        JLabel localLine2 = new JLabel("avec vos amis ou contre des bots");
        localLine1.setFont(descFont); localLine1.setForeground(Color.WHITE);
        localLine2.setFont(descFont); localLine2.setForeground(Color.WHITE);
        localDesc.add(localLine1);
        localDesc.add(localLine2);

        // multi
        JPanel multiDesc = new JPanel();
        multiDesc.setLayout(new BoxLayout(multiDesc, BoxLayout.Y_AXIS));
        multiDesc.setOpaque(false);
        JLabel multiLine1 = new JLabel("Rejoignez ou créez un salon");
        JLabel multiLine2 = new JLabel("pour jouer en ligne avec vos amis");
        multiLine1.setFont(descFont); multiLine1.setForeground(Color.WHITE);
        multiLine2.setFont(descFont); multiLine2.setForeground(Color.WHITE);
        multiDesc.add(multiLine1);
        multiDesc.add(multiLine2);

        gbc.gridx = 0; gbc.gridy = 1; centre.add(localBtn, gbc);
        gbc.gridx = 1; gbc.gridy = 1; centre.add(localDesc, gbc);
        gbc.gridx = 0; gbc.gridy = 2; centre.add(multiBtn, gbc);
        gbc.gridx = 1; gbc.gridy = 2; centre.add(multiDesc, gbc);

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
