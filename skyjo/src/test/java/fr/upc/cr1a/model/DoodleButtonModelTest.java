package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

public class DoodleButtonModelTest {

    private DoodleButtonModel model;

    @BeforeEach
    public void setUp() {
        model = new DoodleButtonModel("Jouer", DoodleButtonModel.ButtonColor.YELLOW, 300, 100);
    }

    @Test
    public void testCreation_SansException() {
        boolean executed = false;
        assertDoesNotThrow(() -> new DoodleButtonModel("Test", DoodleButtonModel.ButtonColor.GREEN, 200, 80));
        executed = true;
        assertTrue(executed, "La creation d'un DoodleButtonModel ne doit pas lancer d'exception");
    }

    @Test
    public void testCreationAvecImagePath() {
        boolean executed = false;
        DoodleButtonModel m = new DoodleButtonModel("Test", DoodleButtonModel.ButtonColor.RED, 200, 80, "/icone.png");
        executed = true;
        assertTrue(executed);
        assertEquals("/icone.png", m.getImagePath());
    }

    @Test
    public void testGetText() {
        assertEquals("Jouer", model.getText(), "getText doit retourner le texte fourni au constructeur");
    }

    @Test
    public void testSetText() {
        model.setText("Paramètres");
        assertEquals("Paramètres", model.getText(), "setText doit modifier le texte");
    }

    @Test
    public void testGetColor_RetourneColorCorrect() {
        Color c = model.getColor();
        assertNotNull(c, "getColor ne doit pas retourner null");
        assertEquals(DoodleButtonModel.ButtonColor.YELLOW.getColor(), c);
    }

    @Test
    public void testGetButtonColor() {
        assertEquals(DoodleButtonModel.ButtonColor.YELLOW, model.getButtonColor());
    }

    @Test
    public void testSetColor() {
        model.setColor(DoodleButtonModel.ButtonColor.RED);
        assertEquals(DoodleButtonModel.ButtonColor.RED, model.getButtonColor());
    }

    @Test
    public void testGetDimensions() {
        assertEquals(300, model.getWidth());
        assertEquals(100, model.getHeight());
    }

    @Test
    public void testGetImagePathNull() {
        assertNull(model.getImagePath(), "imagePath doit etre null si non fourni");
    }

    @Test
    public void testSetImagePath() {
        model.setImagePath("/test/icon.png");
        assertEquals("/test/icon.png", model.getImagePath());
    }

    @Test
    public void testButtonColorEnumYellow() {
        assertNotNull(DoodleButtonModel.ButtonColor.YELLOW.getColor());
    }

    @Test
    public void testButtonColorEnumGreen() {
        assertNotNull(DoodleButtonModel.ButtonColor.GREEN.getColor());
    }

    @Test
    public void testButtonColorEnumRed() {
        assertNotNull(DoodleButtonModel.ButtonColor.RED.getColor());
    }
}
