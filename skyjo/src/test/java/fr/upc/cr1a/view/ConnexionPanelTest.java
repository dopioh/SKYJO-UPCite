package fr.upc.cr1a.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConnexionPanelTest {
    private ConnexionPanel connexionPanel;

    @BeforeEach
    public void setUp() {
        // initialisation du panel avant chaque test
        connexionPanel = new ConnexionPanel();
    }

    @Test
    public void testInitialisationDesBoutons() {
        // verifie que les boutons ne sont pas nuls apres l'initialisation
        assertNotNull(connexionPanel.getbtnSubmitConnexion(), "Le bouton de connexion ne doit pas etre nul");
        assertNotNull(connexionPanel.getbtnSubmitInscription(), "Le bouton d'inscription ne doit pas etre nul");
    }

    @Test
    public void testGettersChampsVidesParDefaut() {
        // Au lancement, les champs doivent être vides
        //assertEquals("", connexionPanel.getLoginEmail(), "L'email de connexion doit être vide par defaut"); // mtn c'est email ou pseudo
        assertEquals("", connexionPanel.getLoginPassword(), "Le mot de passe de connexion doit etre vide par defaut");
        
        assertEquals("", connexionPanel.getInscriptionEmail(), "L'email d'inscription doit etre vide par defaut");
        assertEquals("", connexionPanel.getInscriptionpUsername(), "Le pseudo d'inscripton doit etre vide par defaut");
        assertEquals("", connexionPanel.getInscriptionPassword(), "Le mot de passe d'inscription doit etre vide par defaut");
    }
}
