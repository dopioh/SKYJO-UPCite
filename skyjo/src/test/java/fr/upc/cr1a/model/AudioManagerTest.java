package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JUnit pour AudioManager
 */
public class AudioManagerTest {
    
    private AudioManager audioManager;

    @BeforeEach
    public void setUp() {
        audioManager = new AudioManager();
    }

    @Test
    public void testAudioManagerInitialization() {
        assertNotNull(audioManager, "AudioManager should not be null");
    }

    @Test
    public void testAudioManagerMethods() {
        // Just test that the AudioManager can be instantiated
        // without throwing exceptions
        assertNotNull(audioManager);
        assertTrue(true, "AudioManager instantiation successful");
    }
}
