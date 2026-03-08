package fr.upc.cr1a.view;

import javax.swing.*;
import java.awt.*;

/**
 * Panel qui va demander au joueur de se connecter ou crwer un compte avant d'entrer dans le jeu
 * Panel vraiment simpliste -> a modifier plus tard pour faire un truc plus joli
 */
public class ConnexionPanel extends JPanel {
    
    private CardLayout cardLayout;
    private JPanel cardsContainer;

    // Pour la connexion : 
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField; // pour que ca cache quand on ecrit le mdp
    private JButton btnSubmitConnexion;

    // Pour l'inscription : 
    private JTextField inscriptionEmailField;
    private JTextField inscriptionUsernameField;
    private JPasswordField inscriptionPasswordField;
    private JButton btnSubmitInscription;

    public ConnexionPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE); 

        // gestionnaire de quel pannel est afficher a l'ecran
        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);
        cardsContainer.setOpaque(false);

        // 3 panels pour le moment 
        JPanel selectionCard = selection();
        JPanel loginCard = connexion();
        JPanel inscriptionCard = createAccount();

        // Ajout des pages au conteneur avec un nom de code pour les retrouver
        cardsContainer.add(selectionCard, "SELECTION");
        cardsContainer.add(loginCard, "LOGIN");
        cardsContainer.add(inscriptionCard, "INSCRIPTION");

        add(cardsContainer, BorderLayout.CENTER);

        // Afficher la page de sélection par défaut
        cardLayout.show(cardsContainer, "SELECTION");
    }

    /**
     * Selection entre se connecter ou s'inscrire
     */
    private JPanel selection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        JLabel title = new JLabel("Veuillez creer un compte ou vous connectez si vous en avez deja un.");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        gbc.gridy = 0;
        panel.add(title, gbc);

        JButton btnGoToLogin = new JButton("Se connecter");
        btnGoToLogin.setPreferredSize(new Dimension(200, 40));
        // actionListener juste pour l'animation avant que vous attrapiez ma veste
        btnGoToLogin.addActionListener(e -> cardLayout.show(cardsContainer, "LOGIN")); 

        gbc.gridy = 1;
        panel.add(btnGoToLogin, gbc);

        JButton btnToInscription = new JButton("Créer un compte");
        btnToInscription.setPreferredSize(new Dimension(200, 40));

        btnToInscription.addActionListener(e -> cardLayout.show(cardsContainer, "INSCRIPTION"));

        gbc.gridy = 2;
        panel.add(btnToInscription, gbc);

        return panel;
    }

    /**
     * Panel connexion (pour le moment seulement avec le mail)
     */
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

        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email :"), gbc);
        loginEmailField = new JTextField(15);

        gbc.gridx = 1; 
        gbc.gridy = 1;
        panel.add(loginEmailField, gbc);

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

    /**
     * Panel pour creer un compte
     */
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
        gbc.gridx = 1; gbc.gridy = 1;
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
        gbc.gridy = 4; 
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    // getter

    public String getLoginEmail() { 
        return loginEmailField.getText(); 
    }

    public String getLoginPassword() { 
        return new String(loginPasswordField.getPassword()); // psq les password sont en char[]
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
}