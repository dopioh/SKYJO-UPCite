package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.utils.ImageCache;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel qui va demander au joueur de se connecter ou créer un compte avant d'entrer dans le jeu.
 */
public class ConnexionPanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel cardsContainer;
    private Image backgroundImage;
    private BufferedImage cachedBackground;

    // Pour la connexion :
    private JTextField loginIdentifierField; // anciennement que pour le mail "loginEmailField"
    private JPasswordField loginPasswordField;
    private DoodleButton btnSubmitConnexion;

    // Pour l'inscription :
    private JTextField inscriptionEmailField;
    private JTextField inscriptionUsernameField;

    private JPasswordField inscriptionPasswordField;
    private DoodleButton btnSubmitInscription;

    // Bouton invite
    private DoodleButton btnGuest; // pas de liaison a la base de donnees

    public ConnexionPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        backgroundImage = ImageCache.get("/background.png");

        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);
        cardsContainer.setOpaque(false);

        cardsContainer.add(selection(), "SELECTION");
        cardsContainer.add(connexion(), "LOGIN");
        cardsContainer.add(createAccount(), "INSCRIPTION");

        add(cardsContainer, BorderLayout.CENTER);
        cardLayout.show(cardsContainer, "SELECTION");
    }


    private Font loadFont(String path, float size) {
        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream != null) return Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(size);
        } catch (Exception ignored) {}
        return new Font("Arial", Font.PLAIN, (int) size);
    }

    private JLabel makeText(String text, float size) { // pour mettre des texts dans le DA
        JLabel l = new JLabel(text);
        l.setFont(loadFont("/PatrickHandSC.ttf", size).deriveFont(Font.BOLD));
        l.setForeground(Color.WHITE);
        return l;
    }

    private void styleInput(JTextField f) { // le text qui va dans les champs
        f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(loadFont("/GoogleSans-Regular.ttf", 22f));
    }

    private JTextField styledField(int cols) { // box de texte transparent arrondis
        JTextField f = new JTextField(cols) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleInput(f);
        return f;
    }

    private JPasswordField styledPassword(int cols) { // box de texte transparent arrondis mais pour mdp
        JPasswordField f = new JPasswordField(cols) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleInput(f);
        return f;
    }

    private void addHover(DoodleButton btn) {
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.onMouseEnter(); }
            @Override public void mouseExited(MouseEvent e)  { btn.onMouseExit(); }
        });
    }

    private DoodleButton makeBackButton() {
        DoodleButton btn = new DoodleButton("Retour", ButtonColor.RED, 180, 60);
        btn.setSfxPath("/sfx/back.wav");
        btn.setFontSize(28f);
        addHover(btn);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { cardLayout.show(cardsContainer, "SELECTION"); }
        });
        return btn;
    }


    private JPanel selection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;

        // logo skyjo
        Image scaledLogo = ImageCache.get("/skyjo_logo.png").getScaledInstance(250, -1, Image.SCALE_SMOOTH);
        gbc.gridy = 0; gbc.insets = new Insets(0, 10, 20, 10);
        panel.add(new JLabel(new ImageIcon(scaledLogo)), gbc);

        gbc.insets = new Insets(6, 10, 6, 10);

        // boutton se connecter
        DoodleButton btnGoToLogin = new DoodleButton("Se connecter", ButtonColor.GREEN, 300, 80);
        btnGoToLogin.setFontSize(32f);
        addHover(btnGoToLogin);
        btnGoToLogin.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { cardLayout.show(cardsContainer, "LOGIN"); }
        });
        gbc.gridy = 1;
        panel.add(btnGoToLogin, gbc);

        // boutton créer un compte
        DoodleButton btnToInscription = new DoodleButton("Créer un compte", ButtonColor.YELLOW, 300, 80);
        btnToInscription.setFontSize(32f);
        addHover(btnToInscription);
        btnToInscription.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { cardLayout.show(cardsContainer, "INSCRIPTION"); }
        });
        gbc.gridy = 2;
        panel.add(btnToInscription, gbc);

        // boutton invité
        btnGuest = new DoodleButton("Jouer en invité", ButtonColor.RED, 300, 80);
        btnGuest.setFontSize(32f);
        addHover(btnGuest);
        gbc.gridy = 3;
        panel.add(btnGuest, gbc);

        return panel;
    }

    private JPanel connexion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // titre
        JLabel title = makeText("Connexion", 36f);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        // Email OU Pseudo
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(makeText("Email ou Pseudo :", 26f), gbc);
        loginIdentifierField = styledField(15);
        gbc.gridx = 1;
        panel.add(loginIdentifierField, gbc);

        // Mot de passe
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(makeText("Mot de passe :", 26f), gbc);
        loginPasswordField = styledPassword(15);
        gbc.gridx = 1;
        panel.add(loginPasswordField, gbc);

        // Boutons
        btnSubmitConnexion = new DoodleButton("Valider", ButtonColor.GREEN, 180, 60);
        btnSubmitConnexion.setFontSize(28f);
        addHover(btnSubmitConnexion);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(makeBackButton());
        buttonPanel.add(btnSubmitConnexion);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(24, 10, 8, 10);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createAccount() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // titre
        JLabel title = makeText("Créer un compte", 36f);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        // Email
        gbc.gridx = 0;

        gbc.gridy = 1;
        panel.add(makeText("Email :", 26f), gbc);
        inscriptionEmailField = styledField(15);
        gbc.gridx = 1;
        panel.add(inscriptionEmailField, gbc);

        // Pseudo
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(makeText("Pseudo :", 26f), gbc);
        inscriptionUsernameField = styledField(15);
        gbc.gridx = 1;
        panel.add(inscriptionUsernameField, gbc);

        // Mot de passe
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(makeText("Mot de passe :", 26f), gbc);
        inscriptionPasswordField = styledPassword(15);
        gbc.gridx = 1;
        panel.add(inscriptionPasswordField, gbc);

        // Boutons
        btnSubmitInscription = new DoodleButton("Valider", ButtonColor.GREEN, 180, 60);
        btnSubmitInscription.setFontSize(28f);
        addHover(btnSubmitInscription);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(makeBackButton());
        buttonPanel.add(btnSubmitInscription);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(24, 10, 8, 10);
        panel.add(buttonPanel, gbc);

        return panel;
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

    // getter

    public String getLoginIdentifier() { 
        return loginIdentifierField.getText(); 
    }
    
    public String getLoginPassword() { 
        return new String(loginPasswordField.getPassword()); 
    }

    public DoodleButton getbtnSubmitConnexion() { 
        return btnSubmitConnexion; 
    }

    public String getInscriptionEmail() { 
        return inscriptionEmailField.getText(); 
    }

    public String getInscriptionUsername() {
        return inscriptionUsernameField.getText(); 
    }

    public String getInscriptionPassword() { 
        return new String(inscriptionPasswordField.getPassword()); 
    }

    public DoodleButton getbtnSubmitInscription() { 
        return btnSubmitInscription; 
    }

    public DoodleButton getBtnGuest() { 
        return btnGuest; 
    }

    /**
     * Affiche directement le formulaire de connexion
     */
    public void showLoginForm() {
        cardLayout.show(cardsContainer, "LOGIN");
    }
}