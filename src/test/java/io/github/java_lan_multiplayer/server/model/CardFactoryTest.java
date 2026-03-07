package io.github.java_lan_multiplayer.server.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.java_lan_multiplayer.server.model.cards.Card;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static io.github.java_lan_multiplayer.server.model.BoardType.*;
import static org.junit.jupiter.api.Assertions.*;

public class CardFactoryTest {

    private int countCardsOfLevel(GameModel gameModel, int level) {
        return (int)gameModel.getDeck().stream().filter(c -> c.getLevel() == level).count();
    }

    @Test
    void generateDeck() {
        GameModel learningBoard = new GameModel();
        learningBoard.setBoardType(LEARNING);
        learningBoard.generateDeck();
        GameModel levelOneBoard = new GameModel();
        levelOneBoard.setBoardType(LEVEL_ONE);
        levelOneBoard.generateDeck();
        GameModel levelTwoBoard = new GameModel();
        levelTwoBoard.setBoardType(LEVEL_TWO);
        levelTwoBoard.generateDeck();
        assertEquals(8, learningBoard.getDeck().size(), "Learning decks should contain 8 cards.");
        assertEquals(8, levelOneBoard.getDeck().size(), "Level 1 decks should contain 8 cards.");
        assertEquals(12, levelTwoBoard.getDeck().size(), "Level 2 decks should contain 12 cards.");

        assertEquals(8, countCardsOfLevel(learningBoard, 1), "Learning decks should contain 8 level 1 cards.");
        assertEquals(0, countCardsOfLevel(learningBoard, 2), "Learning decks should contain 0 level 2 cards.");

        assertEquals(8, countCardsOfLevel(levelOneBoard, 1), "Level two decks should contain 4 level 1 cards.");
        assertEquals(0, countCardsOfLevel(levelOneBoard, 2), "Level two decks should contain 8 level 2 cards.");

        assertEquals(4, countCardsOfLevel(levelTwoBoard, 1), "Level two decks should contain 4 level 1 cards.");
        assertEquals(8, countCardsOfLevel(levelTwoBoard, 2), "Level two decks should contain 8 level 2 cards.");
    }

    @Test
    void generateCard() {
        CardFactory factory = CardFactory.getInstance();
        assertEquals("Smugglers",factory.generateCard(LEARNING, 0).getCardType());
        assertEquals("Smugglers",factory.generateCard(LEVEL_ONE, 0).getCardType());
        assertEquals("Slavers",factory.generateCard(LEVEL_TWO, 0).getCardType());
        // The first 8 level 1 cards are the learning cards.
        for(int i = 0; i < 8; i++) {
            assertEquals(factory.generateCard(LEARNING, i).getCardType(), factory.generateCard(LEVEL_ONE, i).getCardType());
        }
        assertThrows(IllegalArgumentException.class, () -> factory.generateCard(LEARNING, -1));
        assertThrows(IllegalArgumentException.class, () -> factory.generateCard(LEARNING, 10));
        assertThrows(IllegalArgumentException.class, () -> factory.generateCard(LEVEL_TWO, -1));
        assertThrows(IllegalArgumentException.class, () -> factory.generateCard(LEARNING, 30));
        assertThrows(IllegalArgumentException.class, () -> factory.generateCard(LEVEL_TWO, 30));
    }

    @Test
    void getInstance_returnsSameInstance() {
        CardFactory instance1 = CardFactory.getInstance();
        CardFactory instance2 = CardFactory.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2, "Should return the same singleton instance.");
    }

    @Test
    void generateDeck_validLearningFlight_returns8Cards() {
        CardFactory factory = CardFactory.getInstance();
        List<Card> deck = factory.generateDeck(LEARNING);

        assertNotNull(deck);
        assertEquals(8, deck.size(), "Deck should contain 8 cards.");
    }

    @Test
    void generateDeck_validLevelTwo_returns12CardsIncludingLevelOne() {
        CardFactory factory = CardFactory.getInstance();
        List<Card> deck = factory.generateDeck(LEVEL_TWO);

        assertEquals(12, deck.size(), "Deck should contain 12 cards (8 level 2 + 4 level 1).");
    }



    @Test
    void generateCard_validId_returnsCorrectCard() {
        CardFactory factory = CardFactory.getInstance();

        // Assuming card with ID 1 exists in LEARNING group
        Card card = factory.generateCard(LEARNING, 1);

        assertNotNull(card);
        assertEquals(1, card.getId(), "Card ID should match.");
    }

    @Test
    void generateCard_invalidId_throwsException() {
        CardFactory factory = CardFactory.getInstance();

        assertThrows(IllegalArgumentException.class, () -> factory.generateCard(LEARNING, -99));
    }

    @Test
    void createCard_unknownType_throwsException() throws Exception {
        JsonNode node = new ObjectMapper().readTree("""
        {
          "id": 999,
          "cardType": "timeWarp",
          "someOtherField": "data"
        }
    """);

        Method method = CardFactory.class.getDeclaredMethod("createCard", JsonNode.class);
        method.setAccessible(true);

        CardFactory factory = CardFactory.getInstance();

        Exception e = assertThrows(InvocationTargetException.class, () -> method.invoke(factory, node));
        assertTrue(e.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void loadJson_cardMissingFields_throwsException() {
        String malformedJson = """
        {
          "learningFlight": [
            { "id": 1 }
          ]
        }
    """;

        assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = new ObjectMapper().readTree(malformedJson);
            for (JsonNode array : root) {
                for (JsonNode card : array) {
                    if (!card.has("id") || !card.has("cardType")) {
                        throw new IllegalArgumentException("Card missing required fields.");
                    }
                }
            }
        });
    }
}
