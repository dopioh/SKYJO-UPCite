package fr.upc.cr1a.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static org.junit.jupiter.api.Assertions.*;

public class CardButtonTest {

    private CardButton cardButton;

    @BeforeEach
    public void setUp() {
        cardButton = new CardButton();
    }

    @Test
    public void testCreation_SansException() {
        boolean executed = false;
        assertDoesNotThrow(() -> new CardButton());
        executed = true;
        assertTrue(executed, "La creation d'un CardButton ne doit pas lancer d'exception");
    }

    @Test
    public void testSetCardImageNull_SansException() {
        boolean executed = false;
        cardButton.setCardImage(null);
        executed = true;
        assertTrue(executed, "setCardImage(null) ne doit pas lancer d'exception");
    }

    @Test
    public void testSetCardImageInexistante_SansException() {
        boolean executed = false;
        cardButton.setCardImage("/fake/inexistant.png");
        executed = true;
        assertTrue(executed, "setCardImage avec une image introuvable ne doit pas lancer d'exception");
    }

    @Test
    public void testSetCardImageDeuxFois_SansException() {
        boolean executed = false;
        cardButton.setCardImage("/fake/image1.png");
        cardButton.setCardImage("/fake/image1.png"); // deuxieme fois = reprise depuis le cache
        executed = true;
        assertTrue(executed, "setCardImage deux fois avec le meme chemin ne doit pas lancer d'exception");
    }

    @Test
    public void testSetAnimationEnabledTrue_SansException() {
        boolean executed = false;
        cardButton.setAnimationEnabled(true);
        executed = true;
        assertTrue(executed, "setAnimationEnabled(true) ne doit pas lancer d'exception");
    }

    @Test
    public void testSetAnimationEnabledFalse_SansException() {
        boolean executed = false;
        cardButton.setAnimationEnabled(false);
        executed = true;
        assertTrue(executed, "setAnimationEnabled(false) ne doit pas lancer d'exception");
    }

    @Test
    public void testSetAnimationEnabledToggle_SansException() {
        boolean executed = false;
        cardButton.setAnimationEnabled(true);
        cardButton.setAnimationEnabled(false);
        executed = true;
        assertTrue(executed, "Activer puis desactiver l'animation ne doit pas lancer d'exception");
    }

    @Test
    public void testMouseEntered_SansException() {
        boolean executed = false;
        MouseEvent e = new MouseEvent(cardButton, MouseEvent.MOUSE_ENTERED, 0, 0, 0, 0, 0, false);
        for (MouseListener ml : cardButton.getMouseListeners()) {
            ml.mouseEntered(e);
        }
        executed = true;
        assertTrue(executed, "mouseEntered ne doit pas lancer d'exception");
    }

    @Test
    public void testMouseExited_SansException() {
        boolean executed = false;
        MouseEvent e = new MouseEvent(cardButton, MouseEvent.MOUSE_EXITED, 0, 0, 0, 0, 0, false);
        for (MouseListener ml : cardButton.getMouseListeners()) {
            ml.mouseExited(e);
        }
        executed = true;
        assertTrue(executed, "mouseExited ne doit pas lancer d'exception");
    }

    @Test
    public void testMousePressed_SansException() {
        boolean executed = false;
        MouseEvent e = new MouseEvent(cardButton, MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, 1, false);
        for (MouseListener ml : cardButton.getMouseListeners()) {
            ml.mousePressed(e);
        }
        executed = true;
        assertTrue(executed, "mousePressed ne doit pas lancer d'exception");
    }

    @Test
    public void testMouseReleased_SansException() {
        boolean executed = false;
        MouseEvent e = new MouseEvent(cardButton, MouseEvent.MOUSE_RELEASED, 0, 0, 0, 0, 1, false);
        for (MouseListener ml : cardButton.getMouseListeners()) {
            ml.mouseReleased(e);
        }
        executed = true;
        assertTrue(executed, "mouseReleased ne doit pas lancer d'exception");
    }

    @Test
    public void testSequenceClicComplete_SansException() {
        boolean executed = false;
        // simule un clic complet : enter -> press -> release -> exit
        MouseEvent enter   = new MouseEvent(cardButton, MouseEvent.MOUSE_ENTERED,  0, 0, 50, 50, 0, false);
        MouseEvent pressed = new MouseEvent(cardButton, MouseEvent.MOUSE_PRESSED,  0, 0, 50, 50, 1, false);
        MouseEvent release = new MouseEvent(cardButton, MouseEvent.MOUSE_RELEASED, 0, 0, 50, 50, 1, false);
        MouseEvent exit    = new MouseEvent(cardButton, MouseEvent.MOUSE_EXITED,   0, 0, 50, 50, 0, false);
        for (MouseListener ml : cardButton.getMouseListeners()) {
            ml.mouseEntered(enter);
            ml.mousePressed(pressed);
            ml.mouseReleased(release);
            ml.mouseExited(exit);
        }
        executed = true;
        assertTrue(executed, "Une sequence de clic complete ne doit pas lancer d'exception");
    }
}
