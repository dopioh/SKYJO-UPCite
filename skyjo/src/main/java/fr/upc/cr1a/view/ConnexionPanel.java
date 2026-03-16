package fr.upc.cr1a.view;

import javax.swing.*;
import java.awt.*;

/**
 * Panel qui va demander au joueur de se connecter ou créer un compte avant d'entrer dans le jeu.
 */
public class ConnexionPanel extends JPanel {
    
    private CardLayout cardLayout;
    private JPanel cardsContainer;

    // Pour la connexion :
    private JTextField loginIdentifierField; // anciennement que pour le mail "loginEmailField"
    private JPasswordField loginPasswordField;
    private JButton btnSubmitConnexion;

    // Pour l'inscription : 
    private JTextField inscriptionEmailField;
    private JTextField inscriptionUsernameField;

    private JPasswordField inscriptionPasswordField;
    private JButton btnSubmitInscription;

    // Bouton invite
    private JButton btnGuest; // pas de liaison a la base de donnees

    public ConnexionPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE); 

        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);
        cardsContainer.setOpaque(false);

        JPanel selectionCard = selection();
        JPanel loginCard = connexion();
        JPanel inscriptionCard = createAccount();

        cardsContainer.add(selectionCard, "SELECTION");
        cardsContainer.add(loginCard, "LOGIN");
        cardsContainer.add(inscriptionCard, "INSCRIPTION");

        add(cardsContainer, BorderLayout.CENTER);

        cardLayout.show(cardsContainer, "SELECTION");
    }

    private JPanel selection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        JLabel title = new JLabel("Bienvenue sur Skyjo !");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        gbc.gridy = 0;
        panel.add(title, gbc);

        JButton btnGoToLogin = new JButton("Se connecter");
        btnGoToLogin.setPreferredSize(new Dimension(200, 40));
        btnGoToLogin.addActionListener(e -> cardLayout.show(cardsContainer, "LOGIN")); 
        gbc.gridy = 1;
        panel.add(btnGoToLogin, gbc);

        JButton btnToInscription = new JButton("Créer un compte");
        btnToInscription.setPreferredSize(new Dimension(200, 40));
        btnToInscription.addActionListener(e -> cardLayout.show(cardsContainer, "INSCRIPTION"));
        gbc.gridy = 2;
        panel.add(btnToInscription, gbc);

        btnGuest = new JButton("Jouer en tant qu'invité");
        btnGuest.setPreferredSize(new Dimension(200, 40));
        gbc.gridy = 3;
        panel.add(btnGuest, gbc);

        return panel;
    }

    private JPanel connexion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Connexion", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; 
        gbc.gridy = 0; 
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        // MODIF : Email OU Pseudo
        gbc.gridx = 0; 
        gbc.gridy = 1;
        panel.add(new JLabel("Email ou Pseudo :"), gbc);
        loginIdentifierField = new JTextField(15);
        gbc.gridx = 1; 
        gbc.gridy = 1;
        panel.add(loginIdentifierField, gbc);

        // Mot de passe
        gbc.gridx = 0; 
        gbc.gridy = 2;
        panel.add(new JLabel("Mot de passe :"), gbc);
        loginPasswordField = new JPasswordField(15);
        gbc.gridx = 1; 
        gbc.gridy = 2;
        panel.add(loginPasswordField, gbc);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        JButton btnBack = new JButton("Retour");
        btnBack.addActionListener(e -> cardLayout.show(cardsContainer, "SELECTION"));
        btnSubmitConnexion = new JButton("Valider");
        
        buttonPanel.add(btnBack);
        buttonPanel.add(btnSubmitConnexion);

        gbc.gridx = 0; 
        gbc.gridy = 3; 
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createAccount() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Créer un compte", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; 
        gbc.gridy = 0; 
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        // Email
        gbc.gridx = 0; 

        gbc.gridy = 1;
        panel.add(new JLabel("Email :"), gbc);
        inscriptionEmailField = new JTextField(15);
        gbc.gridx = 1; 
        gbc.gridy = 1;
        panel.add(inscriptionEmailField, gbc);

        // Pseudo
        gbc.gridx = 0; 
        gbc.gridy = 2;
        panel.add(new JLabel("Pseudo :"), gbc);
        inscriptionUsernameField = new JTextField(15);
        gbc.gridx = 1; 
        gbc.gridy = 2;
        panel.add(inscriptionUsernameField, gbc);

        // Mot de passe
        gbc.gridx = 0; 
        gbc.gridy = 3;
        panel.add(new JLabel("Mot de passe :"), gbc);
        inscriptionPasswordField = new JPasswordField(15);
        gbc.gridx = 1; 
        //gbc.gridy = 2; 
        gbc.gridy = 3;
        panel.add(inscriptionPasswordField, gbc);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        JButton btnBack = new JButton("Retour");

        btnBack.addActionListener(e -> cardLayout.show(cardsContainer, "SELECTION"));

        btnSubmitInscription = new JButton("Valider");
        
        buttonPanel.add(btnBack);
        buttonPanel.add(btnSubmitInscription);

        gbc.gridx = 0; 
        gbc.gridy = 4; // 4 ou 3 ?? // 4
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    // getter 

    public String getLoginIdentifier() { 
        return loginIdentifierField.getText(); 
    }

    public String getLoginPassword() { 
        return new String(loginPasswordField.getPassword()); 
    }

    public JButton getbtnSubmitConnexion() { 
        return btnSubmitConnexion; 
    }

    public String getInscriptionEmail() { 
        return inscriptionEmailField.getText(); 
    }
    
    public String getInscriptionpUsername() { 
        return inscriptionUsernameField.getText(); 
    }

    public String getInscriptionPassword() { 
        return new String(inscriptionPasswordField.getPassword()); 
    }

    public JButton getbtnSubmitInscription() { 
        return btnSubmitInscription; 
    }

    public JButton getBtnGuest() {
        return btnGuest;
    }
}