package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import fr.upc.cr1a.model.player.PlayerData;
import fr.upc.cr1a.model.interfaces.BotStrategyInterface;

/**
 * Tests JUnit pour BotModel
 */
public class BotModelTest {
    
    private BotModel bot;
    private PlayerData botData;

    @BeforeEach
    public void setUp() {
        botData = new PlayerData("Bot1", "bot1@test.com");
        bot = new BotModel(botData);
    }

    @Test
    public void testBotInitialization() {
        assertNotNull(bot);
        assertEquals("Bot1", bot.getName());
    }

    @Test
    public void testBotHand() {
        assertNotNull(bot.getHand());
        assertInstanceOf(BotHandModel.class, bot.getHand());
    }

    @Test
    public void testBotStrategyNull() {
        assertNull(bot.getStrategy(), "Default strategy should be null");
    }

    @Test
    public void testBotWithCustomStrategy() {
        BotStrategyInterface mockStrategy = new BotLow();
        BotModel botWithStrategy = new BotModel(botData, mockStrategy);
        
        assertNotNull(botWithStrategy.getStrategy());
        assertEquals(mockStrategy, botWithStrategy.getStrategy());
    }

    @Test
    public void testSetStrategy() {
        BotStrategyInterface strategy = new BotLow();
        bot.setStrategy(strategy);
        
        assertEquals(strategy, bot.getStrategy());
    }

    @Test
    public void testResetHand() {
        BotHandModel originalHand = bot.getHand();
        bot.resetHand();
        
        assertNotNull(bot.getHand());
        assertNotEquals(originalHand, bot.getHand());
    }

    @Test
    public void testBotScore() {
        assertEquals(0, bot.getTotalScore(), "Initial score should be 0");
        bot.addScore(50);
        assertEquals(50, bot.getTotalScore());
    }

    @Test
    public void testMultipleBots() {
        PlayerData botData2 = new PlayerData("Bot2", "bot2@test.com");
        BotModel bot2 = new BotModel(botData2);
        
        assertNotEquals(bot.getName(), bot2.getName());
        assertNotEquals(bot.getHand(), bot2.getHand());
    }
}
