package io.github.java_lan_multiplayer.server.model.cards;

import io.github.java_lan_multiplayer.server.model.CardFactory;
import io.github.java_lan_multiplayer.server.model.GameModel;
import org.junit.jupiter.api.Test;

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
}
