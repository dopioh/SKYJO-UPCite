package fr.upc.cr1a.controller;

import fr.upc.cr1a.controller.Data.SupabaseAuth;
import fr.upc.cr1a.view.ConnexionPanel;
import fr.upc.cr1a.view.Frame;

/**
 * Classe pour gerer les boutons de ConnexionPanel
 * Appels les fonctions pour se conneter à la base de donnees
 */
public class ConnexionController {

    ConnexionPanel connexionPanel;
    Frame frame;
    
    public ConnexionController(Frame frame, ConnexionPanel connexionPanel){
        this.frame = frame;
        this.connexionPanel = connexionPanel;
        listener();
    }

    public void listener(){
        connexionPanel.getbtnSubmitInscription().addActionListener(e -> sendCreate());
        connexionPanel.getbtnSubmitConnexion().addActionListener(e -> sendLogin());
        
        // boutob invité
        connexionPanel.getBtnGuest().addActionListener(e -> sendGuest());
    }

    public void sendCreate(){
        System.out.println("BOUTON VALIDER INSCRIPTION CALLED");
        String email = connexionPanel.getInscriptionEmail();
        String username = connexionPanel.getInscriptionpUsername();
        String password = connexionPanel.getInscriptionPassword();

        boolean create = SupabaseAuth.createAccount(email, username, password);

        if (!create){
            System.out.println("Erreur lors de l'envoi à la base de données (email/pseudo peut être déjà utilisé)"); 
        } else {
            System.out.println("CONNECTERRRRRRRRRRRR");
            frame.showPanel("MENU");
        }
    }

    public void sendLogin(){
        System.out.println("BOUTON VALIDER CONNEXION CALLED");
        String identifier = connexionPanel.getLoginIdentifier(); // Contient Email OU Pseudo
        String password = connexionPanel.getLoginPassword();

        boolean connexion;

        if (identifier.contains("@")) { // pour diff un pseudo d'un mail
            connexion = SupabaseAuth.connectPlayerByEmail(identifier, password);
        } else {
            connexion = SupabaseAuth.connectPlayerByUsername(identifier, password);
        }

        if (!connexion){
            System.out.println("ERREUR DE CONNEXION : Surement Mot de Passe Errones"); 
        } else {
            System.out.println("CONNNNNNNNECCTERRRRRRRRRRRRRRRRRRRRRRRRRRR");
            frame.showPanel("MENU");
        }
    }


    public void sendGuest(){
        // give un PlayerData vide ? ou rjt un attribut "isInvited" et tout le reste en nul ?
        System.out.println("CONNNNNNNNECCTERRRRRRRRRRRRRRRRRRRRRRRRRRR en tant au'invite");
        frame.showPanel("MENU");
    }
}