package fr.upc.cr1a.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnimLoopTest {

    @AfterEach
    public void tearDown() {
        // nettoyer apres chaque test
        AnimLoop.remove("testKey1");
        AnimLoop.remove("testKey2");
        AnimLoop.remove("testKey3");
    }

    @Test
    public void testAddTask_SansException() {
        boolean executed = false;
        AnimLoop.add("testKey1", dt -> {});
        executed = true;
        assertTrue(executed, "L'ajout d'une tache dans AnimLoop ne doit pas lancer d'exception");
    }

    @Test
    public void testRemoveTask_SansException() {
        boolean executed = false;
        AnimLoop.add("testKey1", dt -> {});
        AnimLoop.remove("testKey1");
        executed = true;
        assertTrue(executed, "La suppression d'une tache dans AnimLoop ne doit pas lancer d'exception");
    }

    @Test
    public void testAddPlusieursTask_SansException() {
        boolean executed = false;
        AnimLoop.add("testKey1", dt -> {});
        AnimLoop.add("testKey2", dt -> {});
        AnimLoop.add("testKey3", dt -> {});
        executed = true;
        assertTrue(executed, "L'ajout de plusieurs taches ne doit pas lancer d'exception");
    }

    @Test
    public void testRemoveCleDoubleNeFaitRien() {
        boolean executed = false;
        AnimLoop.add("testKey1", dt -> {});
        AnimLoop.remove("testKey1");
        AnimLoop.remove("testKey1"); // double remove ne doit pas crasher
        executed = true;
        assertTrue(executed, "La suppression d'une cle deja retiree ne doit pas lancer d'exception");
    }

    @Test
    public void testRemoveCleInexistante_SansException() {
        boolean executed = false;
        AnimLoop.remove("cleQuiExistePas"); // ne doit pas crasher
        executed = true;
        assertTrue(executed, "La suppression d'une cle inexistante ne doit pas lancer d'exception");
    }

    @Test
    public void testAddEtRemoveTousLesElements() {
        boolean executed = false;
        AnimLoop.add("testKey1", dt -> {});
        AnimLoop.add("testKey2", dt -> {});
        AnimLoop.remove("testKey1");
        AnimLoop.remove("testKey2");
        executed = true;
        assertTrue(executed, "Vider completement AnimLoop ne doit pas lancer d'exception");
    }
}
