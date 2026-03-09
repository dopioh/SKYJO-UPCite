/*package fr.upc.cr1a.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import fr.upc.cr1a.controller.Data.SupabaseAuth;

public class SupabaseAuthTest { // marche en local mais pas sur la pipeline

    @Test
    public void testConnectPlayerWithInvalidCredentials() {
        // doit return false 
        boolean result = SupabaseAuth.connectPlayerByEmail("test_invalide@email.com", "mauvais_mdp");
        assertFalse(result, "La connexion avec de mauvais identifiants doit return false");
    }

    @Test
    public void testCreateAccountWithInvalidData() {
        // doit rreturn false
        boolean result = SupabaseAuth.createAccount("", "", "");
        assertFalse(result, "La creation de compte avec des champs vides doit return false");
    }
}
    */
