package fr.upc.cr1a.view;

import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DoodleButtonTest {

    private DoodleButton button;

    @BeforeEach
    public void setUp() {
        button = new DoodleButton("Jouer");
    }

    @Test
    public void testCreationTexte_SansException() {
        boolean executed = false;
        assertDoesNotThrow(() -> new DoodleButton("Jouer"));
        executed = true;
        assertTrue(executed, "La creation d'un DoodleButton avec texte ne doit pas lancer d'exception");
    }

    @Test
    public void testCreationTexteEtCouleur_SansException() {
        boolean executed = false;
        assertDoesNotThrow(() -> new DoodleButton("Boutique", ButtonColor.GREEN));
        executed = true;
        assertTrue(executed, "La creation avec texte et couleur ne doit pas lancer d'exception");
    }

    @Test
    public void testCreationAvecTaille_SansException() {
        boolean executed = false;
        assertDoesNotThrow(() -> new DoodleButton("Paramètres", ButtonColor.YELLOW, 200, 80));
        executed = true;
        assertTrue(executed, "La creation avec taille ne doit pas lancer d'exception");
    }

    @Test
    public void testCreationComplete_SansException() {
        boolean executed = false;
        assertDoesNotThrow(() -> new DoodleButton("Test", ButtonColor.RED, 250, 90, null));
        executed = true;
        assertTrue(executed, "La creation complete ne doit pas lancer d'exception");
    }

    @Test
    public void testOnMouseEnter_SansException() {
        boolean executed = false;
        button.onMouseEnter();
        executed = true;
        assertTrue(executed, "onMouseEnter ne doit pas lancer d'exception");
    }

    @Test
    public void testOnMouseExit_SansException() {
        boolean executed = false;
        button.onMouseExit();
        executed = true;
        assertTrue(executed, "onMouseExit ne doit pas lancer d'exception");
    }

    @Test
    public void testOnMouseEnterSuiviDeExit_SansException() {
        boolean executed = false;
        button.onMouseEnter();
        button.onMouseExit();
        executed = true;
        assertTrue(executed, "onMouseEnter suivi de onMouseExit ne doit pas lancer d'exception");
    }

    @Test
    public void testSetText_SansException() {
        boolean executed = false;
        button.setText("Nouveau texte");
        executed = true;
        assertTrue(executed, "setText ne doit pas lancer d'exception");
    }

    @Test
    public void testSetCouleurYellow_SansException() {
        boolean executed = false;
        button.setCouleur(ButtonColor.YELLOW);
        executed = true;
        assertTrue(executed, "setCouleur(YELLOW) ne doit pas lancer d'exception");
    }

    @Test
    public void testSetCouleurGreen_SansException() {
        boolean executed = false;
        button.setCouleur(ButtonColor.GREEN);
        executed = true;
        assertTrue(executed, "setCouleur(GREEN) ne doit pas lancer d'exception");
    }

    @Test
    public void testSetCouleurRed_SansException() {
        boolean executed = false;
        button.setCouleur(ButtonColor.RED);
        executed = true;
        assertTrue(executed, "setCouleur(RED) ne doit pas lancer d'exception");
    }

}
