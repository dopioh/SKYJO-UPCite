package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JUnit pour TutorialModel
 */
public class TutorialModelTest {
    
    private TutorialModel tutorial;

    @BeforeEach
    public void setUp() {
        tutorial = new TutorialModel();
    }

    @Test
    public void testInitialState() {
        assertNull(tutorial.getCurrentMode(), "Mode should be null initially");
        assertEquals(1, tutorial.getCurrentPageNumber(), "Current page number should be 1");
        assertEquals(0, tutorial.getTotalPages(), "Total pages should be 0 when mode is null");
    }

    @Test
    public void testSetClassicMode() {
        tutorial.setMode(TutorialModel.TutorialMode.CLASSIC);
        assertEquals(TutorialModel.TutorialMode.CLASSIC, tutorial.getCurrentMode());
        assertEquals(1, tutorial.getCurrentPageNumber());
        assertEquals(3, tutorial.getTotalPages());
    }

    @Test
    public void testSetActionMode() {
        tutorial.setMode(TutorialModel.TutorialMode.ACTION);
        assertEquals(TutorialModel.TutorialMode.ACTION, tutorial.getCurrentMode());
        assertEquals(1, tutorial.getCurrentPageNumber());
        assertEquals(3, tutorial.getTotalPages());
    }

    @Test
    public void testGetCurrentPageContent() {
        tutorial.setMode(TutorialModel.TutorialMode.CLASSIC);
        String content = tutorial.getCurrentPageContent();
        assertNotNull(content, "Page content should not be null");
        assertFalse(content.isEmpty(), "Page content should not be empty");
        assertTrue(content.contains("SKYJO CLASSIQUE"), "Classic mode content should contain 'SKYJO CLASSIQUE'");
    }

    @Test
    public void testNextPage() {
        tutorial.setMode(TutorialModel.TutorialMode.CLASSIC);
        assertTrue(tutorial.hasNextPage(), "Should have next page");
        tutorial.nextPage();
        assertEquals(2, tutorial.getCurrentPageNumber());
        tutorial.nextPage();
        assertEquals(3, tutorial.getCurrentPageNumber());
        assertFalse(tutorial.hasNextPage(), "Should not have next page after last");
    }

    @Test
    public void testPreviousPage() {
        tutorial.setMode(TutorialModel.TutorialMode.CLASSIC);
        tutorial.nextPage();
        tutorial.nextPage();
        assertTrue(tutorial.hasPreviousPage(), "Should have previous page");
        tutorial.previousPage();
        assertEquals(2, tutorial.getCurrentPageNumber());
        tutorial.previousPage();
        assertEquals(1, tutorial.getCurrentPageNumber());
        assertFalse(tutorial.hasPreviousPage(), "Should not have previous page at page 1");
    }

    @Test
    public void testReset() {
        tutorial.setMode(TutorialModel.TutorialMode.ACTION);
        tutorial.nextPage();
        tutorial.nextPage();
        tutorial.reset();
        assertEquals(1, tutorial.getCurrentPageNumber());
    }

    @Test
    public void testSwitchMode() {
        tutorial.setMode(TutorialModel.TutorialMode.CLASSIC);
        tutorial.nextPage();
        tutorial.setMode(TutorialModel.TutorialMode.ACTION);
        assertEquals(1, tutorial.getCurrentPageNumber(), "Page should reset to 1 on mode switch");
        assertEquals(TutorialModel.TutorialMode.ACTION, tutorial.getCurrentMode());
    }

    @Test
    public void testActionModeContent() {
        tutorial.setMode(TutorialModel.TutorialMode.ACTION);
        String content = tutorial.getCurrentPageContent();
        assertNotNull(content);
        assertFalse(content.isEmpty());
        assertTrue(content.contains("ACTION"), "Action mode content should contain 'ACTION'");
    }
}
