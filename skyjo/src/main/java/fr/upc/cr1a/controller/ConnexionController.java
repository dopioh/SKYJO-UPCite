package fr.upc.cr1a.controller;

import fr.upc.cr1a.controller.Data.SupabaseAuth;
import fr.upc.cr1a.view.ConnexionPanel;

import fr.upc.cr1a.view.Frame;

public class ConnexionController {

    ConnexionPanel connexionPanel;
    Frame frame;
    
    public ConnexionController(Frame frame,ConnexionPanel connexionPanel){
        this.frame = frame;
        this.connexionPanel = connexionPanel;
        listener();
    }

    public void listener(){
        connexionPanel.getbtnSubmitInscription().addActionListener(e -> sendCreate());
        connexionPanel.getbtnSubmitConnexion().addActionListener(e -> sendLogin());
        connexionPanel.getBtnDev().addActionListener(e -> frame.showPanel("MENU"));
    }

    public void sendCreate(){
        System.out.println("BOUTON VALIDER CALLED");
        String email = connexionPanel.getInscriptionEmail();
        String username = connexionPanel.getInscriptionpUsername();

        boolean create = SupabaseAuth.createAccount(email, username, connexionPanel.getInscriptionPassword()); // dans l'ideal faudrait faire remonter le code http et pas un boolean

        if (!create){
            System.out.println("erreur lors de l'envoie a la base de donnees (email peut etre deja utiliser)"); 
            // lire la reponse SUPABASE pour plus de details 
            // exp : "REPONSE SUPABASE : {"code":422,"error_code":"user_already_exists","msg":"User already registered"}" pour qqln qui a deja un compte
        } else {
            System.out.println("CONNECTERRRRRRRRRRRR");
            frame.showPanel("MENU");
        }
    }

    public void sendLogin(){
        String email = connexionPanel.getLoginEmail();

        boolean connexion = SupabaseAuth.connectPlayerByEmail(email, connexionPanel.getLoginPassword());

        if (!connexion){
            System.out.println("ERREUR DE CONNEXION : Surement Mot de Passe Errones"); 

        } else {
            System.out.println("CONNECTERRRRRRRRRRRR");
            frame.showPanel("MENU");
        }
    }
}
