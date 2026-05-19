package fr.upc.cr1a.controller.network.network_protocol;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JUnit pour les constantes du protocole reseau.
 * Garantit qu'aucune constante n'est null/vide et que les valeurs sont uniques
 * pour eviter les collisions lors du parsing des messages.
 */
public class NetworkProtocolTest {

    @Test
    public void testServerConstantsNotNullOrEmpty() {
        assertNotNull(NetworkProtocolServer.UPDATE_START_GAME);
        assertFalse(NetworkProtocolServer.UPDATE_START_GAME.isBlank());

        assertNotNull(NetworkProtocolServer.UPDATE_LOBBY);
        assertNotNull(NetworkProtocolServer.UPDATE_CARD_FLIPPED);
        assertNotNull(NetworkProtocolServer.UPDATE_TURN_CHANGED);
        assertNotNull(NetworkProtocolServer.UPDATE_PIOCHE_TAKEN);
        assertNotNull(NetworkProtocolServer.UPDATE_DISCARD_TAKEN);
        assertNotNull(NetworkProtocolServer.UPDATE_DISCARD);
        assertNotNull(NetworkProtocolServer.UPDATE_DISCARD_DRAW);
        assertNotNull(NetworkProtocolServer.UPDATE_EXCHANGE);
        assertNotNull(NetworkProtocolServer.UPDATE_KICKED);
        assertNotNull(NetworkProtocolServer.UPDATE_GAME_ENDED);
        assertNotNull(NetworkProtocolServer.UPDATE_ROUND_ENDED);
        assertNotNull(NetworkProtocolServer.UPDATE_NEW_ROUND);
        assertNotNull(NetworkProtocolServer.UPDATE_AFK_REPLACED);
    }

    @Test
    public void testServerConstantsAreUnique() {
        Set<String> values = new HashSet<>();
        values.add(NetworkProtocolServer.UPDATE_START_GAME);
        values.add(NetworkProtocolServer.UPDATE_LOBBY);
        values.add(NetworkProtocolServer.UPDATE_CARD_FLIPPED);
        values.add(NetworkProtocolServer.UPDATE_TURN_CHANGED);
        values.add(NetworkProtocolServer.UPDATE_PIOCHE_TAKEN);
        values.add(NetworkProtocolServer.UPDATE_DISCARD_TAKEN);
        values.add(NetworkProtocolServer.UPDATE_DISCARD);
        values.add(NetworkProtocolServer.UPDATE_DISCARD_DRAW);
        values.add(NetworkProtocolServer.UPDATE_EXCHANGE);
        values.add(NetworkProtocolServer.UPDATE_KICKED);
        values.add(NetworkProtocolServer.UPDATE_GAME_ENDED);
        values.add(NetworkProtocolServer.UPDATE_ROUND_ENDED);
        values.add(NetworkProtocolServer.UPDATE_NEW_ROUND);
        values.add(NetworkProtocolServer.UPDATE_AFK_REPLACED);

        assertEquals(14, values.size(),
            "Toutes les constantes serveur doivent etre uniques");
    }

    @Test
    public void testServerConstantsDoNotContainSeparator() {
        // Le ':' est utilise comme separateur dans les messages
        // Aucune constante de commande ne doit le contenir
        assertFalse(NetworkProtocolServer.UPDATE_START_GAME.contains(":"));
        assertFalse(NetworkProtocolServer.UPDATE_CARD_FLIPPED.contains(":"));
        assertFalse(NetworkProtocolServer.UPDATE_TURN_CHANGED.contains(":"));
        assertFalse(NetworkProtocolServer.UPDATE_EXCHANGE.contains(":"));
        assertFalse(NetworkProtocolServer.UPDATE_GAME_ENDED.contains(":"));
        assertFalse(NetworkProtocolServer.UPDATE_AFK_REPLACED.contains(":"));
    }

    @Test
    public void testStartGameValue() {
        assertEquals("START_GAME", NetworkProtocolServer.UPDATE_START_GAME);
    }

    @Test
    public void testGameEndedValue() {
        assertEquals("GAME_ENDED", NetworkProtocolServer.UPDATE_GAME_ENDED);
    }

    @Test
    public void testAfkReplacedValue() {
        assertEquals("AFK_REPLACED", NetworkProtocolServer.UPDATE_AFK_REPLACED);
    }

    @Test
    public void testNewRoundValue() {
        assertEquals("NEW_ROUND", NetworkProtocolServer.UPDATE_NEW_ROUND);
    }

    //NetworkProtocolClient

    @Test
    public void testClientConstantsNotNullOrEmpty() {
        assertNotNull(NetworkProtocolClient.REQ_FLIP_CARD);
        assertFalse(NetworkProtocolClient.REQ_FLIP_CARD.isBlank());

        assertNotNull(NetworkProtocolClient.REQ_PICK_PIOCHE);
        assertNotNull(NetworkProtocolClient.REQ_PICK_DEFAUSSE);
        assertNotNull(NetworkProtocolClient.REQ_DISCARD);
        assertNotNull(NetworkProtocolClient.REQ_DISCARD_DRAW);
        assertNotNull(NetworkProtocolClient.REQ_READY);
    }

    @Test
    public void testClientConstantsAreUnique() {
        Set<String> values = new HashSet<>();
        values.add(NetworkProtocolClient.REQ_FLIP_CARD);
        values.add(NetworkProtocolClient.REQ_PICK_PIOCHE);
        values.add(NetworkProtocolClient.REQ_PICK_DEFAUSSE);
        values.add(NetworkProtocolClient.REQ_DISCARD);
        values.add(NetworkProtocolClient.REQ_DISCARD_DRAW);
        values.add(NetworkProtocolClient.REQ_READY);

        assertEquals(6, values.size(),
            "Toutes les constantes client doivent etre uniques");
    }

    @Test
    public void testDiscardAndDiscardDrawDistinguishable() {
        // Test specifique : REQ_DISCARD est un PREFIXE de REQ_DISCARD_DRAW
        // Le parsing doit utiliser ":" + REQ_DISCARD + ":" pour eviter les collisions
        assertTrue(NetworkProtocolClient.REQ_DISCARD_DRAW.startsWith(NetworkProtocolClient.REQ_DISCARD),
            "REQ_DISCARD_DRAW commence par REQ_DISCARD — le parsing doit etre attentif");
    }
}
