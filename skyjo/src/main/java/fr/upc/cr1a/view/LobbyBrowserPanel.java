package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.model.LobbyModel;
import fr.upc.cr1a.utils.ImageCache;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *panel pour afficher et parcourir les salons multijoueur disponibles
 *
 */
public class LobbyBrowserPanel extends JPanel {

    private DoodleButton backBtn;
    private DoodleButton createLobbyBtn;
    private DoodleButton refreshBtn;
    private JPanel lobbiesListPanel;
    private JScrollPane scrollPane;
    private Image backgroundImage;
    private BufferedImage cachedBackground;
    private List<LobbyItemPanel> lobbyItems;
    private Font doodle;

    public LobbyBrowserPanel() {
        setLayout(new BorderLayout());
        lobbyItems = new ArrayList<>();
        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            doodle = stream != null ? Font.createFont(Font.TRUETYPE_FONT, stream) : new Font("Arial", Font.PLAIN, 12);
        } catch (Exception e) {
            doodle = new Font("Arial", Font.PLAIN, 12);
        }

        //fond
        backgroundImage = ImageCache.get("/background.png");

        //barre supérieure avec boutons
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        //zone centrale avec liste des salons
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        //bouton retour à gauche
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);
        backBtn = new DoodleButton("Retour", ButtonColor.RED, 220, 60, "/Button/retour.png");
        backBtn.setSfxPath("/sfx/back.wav");
        backBtn.setFontSize(32f);
        backBtn.setIconSide(DoodleButton.IconSide.LEFT);
        leftPanel.add(backBtn);

        //boutons à droite
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        refreshBtn = new DoodleButton("Rafraîchir", ButtonColor.YELLOW, 220, 60);
        refreshBtn.setFontSize(32f);
        createLobbyBtn = new DoodleButton("Créer un salon", ButtonColor.GREEN, 300, 60);
        createLobbyBtn.setFontSize(32f);
        rightPanel.add(refreshBtn);
        rightPanel.add(createLobbyBtn);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 40, 40, 40);

        //panel contenant la liste des salons
        lobbiesListPanel = new JPanel();
        lobbiesListPanel.setLayout(new BoxLayout(lobbiesListPanel, BoxLayout.Y_AXIS));
        lobbiesListPanel.setOpaque(false);

        //scrollPane pour la liste
        scrollPane = new JScrollPane(lobbiesListPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        centerPanel.add(scrollPane, gbc);

        return centerPanel;
    }

    /**
     *ajoute un salon à la liste
     */
    public void addLobby(LobbyItemPanel lobbyItem) {
        lobbyItems.add(lobbyItem);
        lobbiesListPanel.add(lobbyItem);
        lobbiesListPanel.add(Box.createVerticalStrut(10));
        lobbiesListPanel.revalidate();
        lobbiesListPanel.repaint();
    }

    /**
     * vide la liste des salons
     */
    public void clearLobbies() {
        lobbyItems.clear();
        lobbiesListPanel.removeAll();
        lobbiesListPanel.revalidate();
        lobbiesListPanel.repaint();
    }

    /**
     * rafraîchit la liste avec de nouveaux salons
     */
    public void refreshLobbies(List<LobbyModel> lobbies) {
        clearLobbies();
        for (LobbyModel lobby : lobbies) {
            LobbyItemPanel lobbyItem = new LobbyItemPanel(lobby);
            addLobby(lobbyItem);
        }
    }

    /**
     * affiche un message quand aucun salon n'est disponible
     */
    public void showNoLobbiesMessage() {
        clearLobbies();
        JLabel noLobbiesLabel = new JLabel("Aucun salon disponible pour le moment");
        noLobbiesLabel.setFont(doodle.deriveFont(Font.BOLD, 24f));
        noLobbiesLabel.setForeground(Color.WHITE);
        noLobbiesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel createLobbyHint = new JLabel("Créez un nouveau salon pour commencer à jouer !");
        createLobbyHint.setFont(doodle.deriveFont(Font.BOLD, 18f));
        createLobbyHint.setForeground(new Color(220, 220, 220));
        createLobbyHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(false);
        messagePanel.add(Box.createVerticalGlue());
        messagePanel.add(noLobbiesLabel);
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.add(createLobbyHint);
        messagePanel.add(Box.createVerticalGlue());

        lobbiesListPanel.add(messagePanel);
        lobbiesListPanel.revalidate();
        lobbiesListPanel.repaint();
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

    //getters
    public DoodleButton getBackButton() {
        return backBtn;
    }

    public DoodleButton getCreateLobbyButton() {
        return createLobbyBtn;
    }

    public DoodleButton getRefreshButton() {
        return refreshBtn;
    }

    public List<LobbyItemPanel> getLobbyItems() {
        return lobbyItems;
    }
}
