package io.github.java_lan_multiplayer.server.model;

import io.github.java_lan_multiplayer.server.model.tiles.CrewType;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Arrays;

import static io.github.java_lan_multiplayer.DefaultShipBoards.defaultShipBoard;
import static io.github.java_lan_multiplayer.server.model.Player.PlayerState.*;
import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {

    @Test
    void addPlayer() {
        GameModel gameModel = new GameModel();
        Player player1 = new Player("Test Player 1", new int[]{0, 0});
        Player player1b = new Player("Test Player 1", new int[]{0, 0});
        Player player2 = new Player("Test Player 2", new int[]{0, 0});
        Player player3 = new Player("Test Player 3", new int[]{0, 0});
        Player player4 = new Player("Test Player 4", new int[]{0, 0});
        Player player5 = new Player("Test Player 5", new int[]{0, 0});
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);
        assertThrows(IllegalStateException.class, () -> gameModel.addPlayer(player5), "Lobby should be full.");
        gameModel.removePlayer(player3);
        assertThrows(IllegalArgumentException.class, () -> gameModel.addPlayer(player1), "playeris already in the game.");
        assertThrows(IllegalStateException.class, () -> gameModel.addPlayer(player1b), "There should be already a player with that name.");
        gameModel.addPlayer(player5);
    }

    @Test
    void removePlayer() {
        GameModel gameModel = new GameModel();
        Player player1 = new Player("Test Player 1", new int[]{0, 0});
        Player player2 = new Player("Test Player 2", new int[]{0, 0});
        Player player3 = new Player("Test Player 3", new int[]{0, 0});
        assertThrows(IllegalArgumentException.class, () -> gameModel.removePlayer(player1), "player1 is not in the game yet.");
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        assertThrows(IllegalArgumentException.class, () -> gameModel.removePlayer(player3), "player3 is not in the game.");
        assertEquals(2, gameModel.getSortedPlayers().size(), "There should be 2 players in the game.");
        gameModel.removePlayer(player2);
        assertEquals(1, gameModel.getSortedPlayers().size(), "There should be 1 player in the game.");
        gameModel.removePlayer(player1);
        assertEquals(0, gameModel.getSortedPlayers().size(), "There should be no players in the game.");
        assertThrows(IllegalArgumentException.class, () -> gameModel.removePlayer(player1), "player1 is not in the game anymore.");
    }

    @Test
    void updatePlayersColor() {
        GameModel gameModel = new GameModel();
        Player player1 = new Player("Test Player 1", new int[]{0, 0});
        Player player2 = new Player("Test Player 2", new int[]{0, 0});
        Player player3 = new Player("Test Player 3", new int[]{0, 0});
        Player player4 = new Player("Test Player 4", new int[]{0, 0});
        gameModel.addPlayer(player1);
        assertEquals(Player.Color.BLUE, player1.getColor(), "player1 color should be BLUE.");
        gameModel.addPlayer(player2);
        assertEquals(Player.Color.RED, player2.getColor(), "player2 color should be RED.");
        gameModel.addPlayer(player3);
        assertEquals(Player.Color.GREEN, player3.getColor(), "player3 color should be GREEN.");
        gameModel.addPlayer(player4);
        assertEquals(Player.Color.YELLOW, player4.getColor(), "player4 color should be YELLOW.");
        gameModel.removePlayer(player1);
        assertEquals(Player.Color.RED, player2.getColor(), "player2 color should still be RED.");
        assertEquals(Player.Color.GREEN, player3.getColor(), "player3 color should still be GREEN.");
        assertEquals(Player.Color.YELLOW, player4.getColor(), "player4 color should still be YELLOW.");
    }

    @Test
    void getSortedPlayers() {
        GameModel gameModel = new GameModel();
        Player player1 = new Player("Test Player 1", new int[]{0, 0});
        Player player2 = new Player("Test Player 2", new int[]{0, 0});
        Player player3 = new Player("Test Player 3", new int[]{0, 0});
        Player player4 = new Player("Test Player 4", new int[]{0, 0});
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);

        assertEquals(Arrays.asList(player1, player2, player3, player4), gameModel.getSortedPlayers(), "everyone has equal score, so the original order is maintained");
        assertNotEquals(Arrays.asList(player4, player3, player2, player1), gameModel.getSortedPlayers(), "everyone has equal score, so the original order is maintained");

        gameModel.setPlayerPosition(player1, 10);
        gameModel.setPlayerPosition(player2, 1);
        gameModel.setPlayerPosition(player3, 7);
        gameModel.setPlayerPosition(player4, 5);

        assertEquals(Arrays.asList(player1, player3, player4, player2), gameModel.getSortedPlayers(), "everyone has equal score, so the original order is maintained");
    }

    @Test
    void setPlayerPosition() {
        GameModel gameModel = new GameModel();
        Player player1 = new Player("Test Player 1", new int[]{0, 0});
        Player player2 = new Player("Test Player 2", new int[]{0, 0});
        Player player3 = new Player("Test Player 3", new int[]{0, 0});
        Player player4 = new Player("Test Player 4", new int[]{0, 0});
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);

        gameModel.setPlayerPosition(player1, 10);
        assertEquals(10, player1.getPosition(), "player1 should now be at position 10.");
        // should be able to not move a player.
        gameModel.setPlayerPosition(player1, 10);
        gameModel.setPlayerPosition(player2, -6);
        assertEquals(-6, player2.getPosition(), "player1 should now be at position -6.");
        gameModel.setPlayerPosition(player3, 7);
        assertEquals(7, player3.getPosition(), "player1 should now be at position 7.");
        gameModel.setPlayerPosition(player4, 5);
        assertEquals(5, player4.getPosition(), "player1 should now be at position 5.");
        
        assertThrows(IllegalArgumentException.class, () -> gameModel.setPlayerPosition(player1, 7), "Position 7 should be occupied.");
        assertEquals(10, player1.getPosition(), "player1 should still be at position 10.");
        
        player3.setState(ELIMINATED);
        assertThrows(IllegalStateException.class, () -> gameModel.setPlayerPosition(player3, 0), "player3 should be eliminated.");
    }

    @Test
    void movePlayerForward() {
        GameModel gameModel = new GameModel();
        Player player1 = new Player("Test Player 1", new int[]{0, 0});
        Player player2 = new Player("Test Player 2", new int[]{0, 0});
        Player player3 = new Player("Test Player 3", new int[]{0, 0});
        Player player4 = new Player("Test Player 4", new int[]{0, 0});
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);

        player1.setPosition(10);
        player2.setPosition(1);
        player3.setPosition(7);
        player4.setPosition(5);

        gameModel.movePlayerBackward(player2,3);
        assertEquals(-2,player2.getPosition(),"player2's position should now be -2");
        gameModel.movePlayerBackward(player1,5);
        assertEquals(3,player1.getPosition(),"player1's position should now be 3");
        gameModel.movePlayerBackward(player1,0);
        assertEquals(3,player1.getPosition(),"player1's position should still be 3");
        gameModel.movePlayerBackward(player3,5);
        assertEquals(0,player3.getPosition(),"player3's position should now be 0");
    }

    @Test
    void movePlayerBackward() {
        GameModel gameModel = new GameModel();
        Player player1 = new Player("Test Player 1", new int[]{0, 0});
        Player player2 = new Player("Test Player 2", new int[]{0, 0});
        Player player3 = new Player("Test Player 3", new int[]{0, 0});
        Player player4 = new Player("Test Player 4", new int[]{0, 0});
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);

        player1.setPosition(10);
        player2.setPosition(1);
        player3.setPosition(7);
        player4.setPosition(5);

        gameModel.movePlayerBackward(player2,3);
        assertEquals(-2,player2.getPosition(),"player2's position should now be -2");
        gameModel.movePlayerBackward(player1,5);
        assertEquals(3,player1.getPosition(),"player1's position should now be 3");
        gameModel.movePlayerBackward(player1,0);
        assertEquals(3,player1.getPosition(),"player1's position should still be 3");
        gameModel.movePlayerBackward(player3,5);
        assertEquals(0,player3.getPosition(),"player3's position should now be 0");
    }

    @Test
    void updatePlayersStatus() {
        GameModel gameModel = new GameModel();
        Player player1 = new Player("Test Player 1", new int[]{0, 0}, defaultShipBoard(1));
        Player player2 = new Player("Test Player 2", new int[]{0, 0}, defaultShipBoard(2));
        Player player3 = new Player("Test Player 3", new int[]{0, 0}, defaultShipBoard(3));
        Player player4 = new Player("Test Player 4", new int[]{0, 0}, defaultShipBoard(4));
        player4.getShipBoard().setCrewTypeAt(4,3, CrewType.PURPLE_ALIEN);
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);

        assertNotEquals(ELIMINATED, player1.getState(), "player1 should not be eliminated.");
        assertNotEquals(ELIMINATED, player2.getState(), "player2 should not be eliminated.");
        assertNotEquals(ELIMINATED, player3.getState(), "player3 should not be eliminated.");
        assertNotEquals(ELIMINATED, player4.getState(), "player4 should not be eliminated.");

        gameModel.updatePlayersStatus();
        assertNotEquals(ELIMINATED, player1.getState(), "player1 should still not be eliminated.");
        assertNotEquals(ELIMINATED, player2.getState(), "player2 should still not be eliminated.");
        assertNotEquals(ELIMINATED, player3.getState(), "player3 should still not be eliminated.");
        assertNotEquals(ELIMINATED, player4.getState(), "player4 should still not be eliminated.");

        // make player2 more than 24 spaces behind player3
        gameModel.setPlayerPosition(player1, 12);
        gameModel.setPlayerPosition(player2, -15);
        gameModel.setPlayerPosition(player3, 15);
        gameModel.setPlayerPosition(player4, 1);

        gameModel.updatePlayersStatus();
        assertNotEquals(ELIMINATED, player1.getState(), "player1 should still not be eliminated.");
        assertEquals(ELIMINATED, player2.getState(), "player2 should now be eliminated (lapped).");
        assertNotEquals(ELIMINATED, player3.getState(), "player3 should still not be eliminated.");
        assertNotEquals(ELIMINATED, player4.getState(), "player4 should still not be eliminated.");

        gameModel.setPlayerPosition(player1, 0);
        gameModel.setPlayerPosition(player3, 2);
        gameModel.setPlayerPosition(player4, -3);

        // removes all crew members from player4
        player4.ensureCrewRemovedAt(new Point(3,2));
        player4.ensureCrewRemovedAt(new Point(3,2));
        player4.ensureCrewRemovedAt(new Point(4,2));
        player4.ensureCrewRemovedAt(new Point(4,2));
        assertEquals(0, player4.getHumanCount(), "player4's human count should be 0");
        assertEquals(1, player4.getCrewCount(), "player4's crew count should be 1 (purple alien)");

        gameModel.updatePlayersStatus();
        assertNotEquals(ELIMINATED, player1.getState(), "player1 should still not be eliminated.");
        assertEquals(ELIMINATED, player2.getState(), "player2 should still be eliminated.");
        assertNotEquals(ELIMINATED, player3.getState(), "player3 should still not be eliminated.");
        assertEquals(ELIMINATED, player4.getState(), "player4 should now be eliminated (0 crew members).");
    }
}