package io.github.java_lan_multiplayer.server.model.cards;

import io.github.java_lan_multiplayer.server.model.GameModel;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.projectile.CannonShot;
import io.github.java_lan_multiplayer.server.model.cards.projectile.Meteor;
import io.github.java_lan_multiplayer.server.model.cards.projectile.Projectile;
import io.github.java_lan_multiplayer.server.model.tiles.*;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.*;
import java.util.List;

import static io.github.java_lan_multiplayer.server.model.BoardType.*;
import static io.github.java_lan_multiplayer.server.model.cards.CombatZoneCard.CombatRule.*;
import static io.github.java_lan_multiplayer.server.model.tiles.BlockType.*;
import static io.github.java_lan_multiplayer.server.model.tiles.Connector.*;
import static io.github.java_lan_multiplayer.DefaultShipBoards.defaultShipBoard;
import static org.junit.jupiter.api.Assertions.*;

class CardsTest {

    public static final Connector[] FULL_UNIVERSAL_CONNECTORS = {UNIVERSAL, UNIVERSAL, UNIVERSAL, UNIVERSAL};

    @Test
    void abandonedShip() {
        AbandonedShipCard abShipCard = new AbandonedShipCard(0,2, 6,3, 3);

        GameModel gameModel = new GameModel();
        Player player1 = new Player("Test Player 1", new int[]{0, 0}, defaultShipBoard(1));
        Player player2 = new Player("Test Player 2", new int[]{0, 0}, defaultShipBoard(2));
        Player player3 = new Player("Test Player 3", new int[]{0, 0}, defaultShipBoard(3));
        Player player4 = new Player("Test Player 4", new int[]{0, 0}, defaultShipBoard(4));
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);

        gameModel.setPlayerPosition(player4, 3);
        gameModel.setPlayerPosition(player3, 2);
        gameModel.setPlayerPosition(player2, 1);
        gameModel.setPlayerPosition(player1, 0);

        assertEquals(6, player4.getCrewCount(), "player4's crew count should be 6.");
        assertEquals(12, player3.getCrewCount(), "player3's crew count should be 11.");
        assertEquals(8, player2.getCrewCount(), "player2's crew count should be 8.");
        assertEquals(8, player1.getCrewCount(), "player1's crew count should be 8.");

        abShipCard.activate(gameModel);
        // The player order is player4, player3, player2, player1.
        assertThrows(IllegalStateException.class, () -> abShipCard.accept(player4), "player4's turn should have been skipped.");
        assertThrows(IllegalStateException.class, () -> abShipCard.refuse(player4), "player4's turn should have been skipped.");

        abShipCard.refuse(player3);

        assertEquals(1, player2.getPosition(), "player2 should be at position 1.");
        assertEquals(0, player2.getCredits(), "player2 should have 0 credits.");

        abShipCard.accept(player2);

        abShipCard.removeMemberFrom(player2, new Point(1,2));
        abShipCard.removeMemberFrom(player2, new Point(1,2));
        abShipCard.removeMemberFrom(player2, new Point(1,3));
        abShipCard.removeMemberFrom(player2, new Point(1,3));
        abShipCard.removeMemberFrom(player2, new Point(3,2));
        abShipCard.removeMemberFrom(player2, new Point(5,2));
        assertThrows(IllegalStateException.class, () -> abShipCard.removeMemberFrom(player2, new Point(3,2)), "The player has already removed all the required members.");

        assertEquals(-3, player2.getPosition(), "player2 should be at position -3.");
        assertEquals(3, player2.getCredits(), "player2 should have gained 3 credits.");

        assertThrows(IllegalStateException.class, () -> abShipCard.refuse(player1), "player2 already accepted the rewards.");

        assertTrue(abShipCard.isFinished(), "The card should be finished.");
    }

    @Test
    void abandonedStation() {
        BlockType[] reward = {RED, GREEN, YELLOW, YELLOW};
        AbandonedStationCard abStationCard = new AbandonedStationCard(0,2, 8, reward, 3);

        GameModel gameModel = new GameModel();
        Player player1 = new Player("Test Player 1", new int[]{0, 0}, defaultShipBoard(1));
        Player player2 = new Player("Test Player 2", new int[]{0, 0}, defaultShipBoard(2));
        Player player3 = new Player("Test Player 3", new int[]{0, 0}, defaultShipBoard(3));
        Player player4 = new Player("Test Player 4", new int[]{0, 0}, defaultShipBoard(4));
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);

        gameModel.setPlayerPosition(player4, 3);
        gameModel.setPlayerPosition(player3, 2);
        gameModel.setPlayerPosition(player2, 1);
        gameModel.setPlayerPosition(player1, 0);

        assertEquals(6, player4.getCrewCount(), "player4's crew count should be 6.");
        assertEquals(12, player3.getCrewCount(), "player3's crew count should be 11.");
        assertEquals(8, player2.getCrewCount(), "player2's crew count should be 8.");
        assertEquals(8, player1.getCrewCount(), "player1's crew count should be 8.");

        abStationCard.activate(gameModel);
        // The player order is player4, player3, player2, player1.
        assertThrows(IllegalStateException.class, () -> abStationCard.accept(player4), "player4's turn should have been skipped.");
        assertThrows(IllegalStateException.class, () -> abStationCard.refuse(player4), "player4's turn should have been skipped.");

        abStationCard.refuse(player3);

        assertEquals(1, player2.getPosition(), "player2 should be at position 1.");
        assertEquals(0, player2.getCargoHoldsValue(), "player2 held value should be 0.");

        abStationCard.accept(player2);
        assertEquals(List.of(RED, GREEN, YELLOW, YELLOW), player2.getCargoPool());

        abStationCard.loadCargo(player2, 1, new Point(4, 1), 0);
        assertEquals(List.of(RED, YELLOW, YELLOW), player2.getCargoPool());
        assertEquals(2, player2.getCargoHoldsValue(), "player2 held value should now be 2.");

        abStationCard.loadCargo(player2, 2, new Point(4, 3), 0);
        assertEquals(List.of(RED, YELLOW), player2.getCargoPool());
        assertEquals(5, player2.getCargoHoldsValue(), "player2 held value should now be 5.");

        abStationCard.loadCargo(player2, 1, new Point(4, 1), 0);
        assertEquals(List.of(RED, GREEN), player2.getCargoPool());
        assertEquals(6, player2.getCargoHoldsValue(), "player2 held value should now be 6.");

        abStationCard.confirmDone(player2);
        assertThrows(IllegalStateException.class, () -> abStationCard.loadCargo(player2, 1, new Point(4, 1), 0), "player cannot place cargo anymore.");

        assertEquals(-3, player2.getPosition(), "player2 should be at position -3.");
        assertEquals(6, player2.getCargoHoldsValue(), "player2 held value should now be 6.");

        assertThrows(IllegalStateException.class, () -> abStationCard.refuse(player1), "player2 already accepted the rewards.");

        assertTrue(abStationCard.isFinished(), "The card should be finished.");
    }

    @Test
    void combatZone() {
        CannonShot[] cannonShots = new CannonShot[]{
                new CannonShot(Projectile.Size.SMALL, Projectile.Source.UP, 1),
                new CannonShot(Projectile.Size.LARGE,Projectile.Source.UP, 1),
                new CannonShot(Projectile.Size.SMALL,Projectile.Source.DOWN, 1)
        };
        CombatZoneCard.CombatRule rule1 = new CombatZoneCard.CombatRule(Condition.CANNON_POWER,Punishment.FLIGHT_DAYS,3,null);
        CombatZoneCard.CombatRule rule2 = new CombatZoneCard.CombatRule(Condition.ENGINE_POWER,Punishment.CANNON_SHOTS,3,cannonShots);
        CombatZoneCard.CombatRule rule3 = new CombatZoneCard.CombatRule(Condition.CREW_SIZE,Punishment.CARGO_LOSS,3,null);
        CombatZoneCard.CombatRule rule4 = new CombatZoneCard.CombatRule(Condition.CANNON_POWER,Punishment.CREW_LOSS,3,null);

        CombatZoneCard.CombatRule[] combatRules = new CombatZoneCard.CombatRule[]{rule1,rule2,rule3,rule4};

        CombatZoneCard combatZoneCard = new CombatZoneCard(0,2,combatRules);

        GameModel gameModel = new GameModel();
        Player player1 = new Player("Test Player 1", new int[]{0, 0}, defaultShipBoard(1));
        Player player2 = new Player("Test Player 2", new int[]{0, 0}, defaultShipBoard(2));
        Player player3 = new Player("Test Player 3", new int[]{0, 0}, defaultShipBoard(3));
        Player player4 = new Player("Test Player 4", new int[]{0, 0}, defaultShipBoard(4));
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);
        // settingAliens
        player1.getShipBoard().setCrewTypeAt(2,2,CrewType.BROWN_ALIEN);
        player2.getShipBoard().setCrewTypeAt(1,3,CrewType.BROWN_ALIEN);
        player3.getShipBoard().setCrewTypeAt(4,2,CrewType.BROWN_ALIEN);
        player4.getShipBoard().setCrewTypeAt(4,3,CrewType.PURPLE_ALIEN);
        // loading cargoHolds
        player1.getShipBoard().loadCargoAt(2,1, 0, BlockType.BLUE);
        player2.getShipBoard().loadCargoAt(4,1, 0, BlockType.BLUE);
        player3.getShipBoard().loadCargoAt(5,2, 0, BlockType.BLUE);
        player4.getShipBoard().loadCargoAt(2,1, 1, BlockType.YELLOW);
        player4.getShipBoard().loadCargoAt(1,3, 1, BlockType.BLUE);

        gameModel.movePlayerForward(player4, 3);      // 3
        gameModel.movePlayerForward(player3, 3);      // 4
        gameModel.movePlayerForward(player2, 3);      // 5
        gameModel.movePlayerForward(player1, 3);      // 6

        assertEquals(6,player1.getPosition(),"player1 should start at position 6.");
        assertEquals(5,player2.getPosition(),"player2 should start at position 5.");
        assertEquals(4,player3.getPosition(),"player3 should start at position 4.");
        assertEquals(3,player4.getPosition(),"player4 should start at position 3.");

        assertEquals(1,player1.getShipBoard().getTotalHoldsValue(),"player1 should start with a total holds value of 1.");
        assertEquals(1,player2.getShipBoard().getTotalHoldsValue(),"player2 should start with a total holds value of 1.");
        assertEquals(1,player3.getShipBoard().getTotalHoldsValue(),"player3 should start with a total holds value of 1.");
        assertEquals(4,player4.getShipBoard().getTotalHoldsValue(),"player4 should start with a total holds value of 4.");
        assertEquals(12,player4.getShipBoard().getTotalBatteriesCount(), "player4 should start with 12 batteries.");

        // apply the card
        combatZoneCard.activate(gameModel);

        // rule 1
        combatZoneCard.activateCannons(player1, 0, 0, new ArrayList<>());
        combatZoneCard.activateCannons(player2, 0, 0, new ArrayList<>());
        combatZoneCard.activateCannons(player3, 0, 0, new ArrayList<>());
        combatZoneCard.activateCannons(player4, 0, 0, new ArrayList<>());
        // The worst player is player3
        assertEquals(6,player1.getPosition(),"player1 should still be at position 6.");
        assertEquals(5,player2.getPosition(),"player2 should still be at position 5.");
        assertEquals(0,player3.getPosition(),"player3 should now be at position 0.");
        assertEquals(3,player4.getPosition(),"player4 should still be at position 3.");

        // rule 2
        assertEquals(26, player1.getTotalTileCount(),"player1 should start with 26 tiles.");
        assertEquals(25, player2.getTotalTileCount(),"player2 should start with 25 tiles.");
        assertEquals(25, player3.getTotalTileCount(),"player3 should start with 25 tiles.");
        assertEquals(22, player4.getTotalTileCount(),"player4 should start with 22 tiles.");
        assertInstanceOf(CannonTile.class, player4.getShipBoard().getTile(0, 4), "player4 should have a Cannon at (0,4).");

        // no one activates their double engines. only aliens and single engines are used.
        combatZoneCard.activateEngines(player1, 0, new ArrayList<>());
        combatZoneCard.activateEngines(player2, 0, new ArrayList<>());
        combatZoneCard.activateEngines(player4, 0, new ArrayList<>());
        combatZoneCard.activateEngines(player3, 0, new ArrayList<>());
        // player3 and player4 have the same score (3), but player4 appear first.
        // player4 does not have shields to protect himself and loses 5 tiles.
        // It also loses 2 batteries and a blue cargo.
        assertEquals(26, player1.getTotalTileCount(),"player1 should still have all 26 tiles.");
        assertEquals(25, player2.getTotalTileCount(),"player2 should still have all 25 tiles.");
        assertEquals(25, player3.getTotalTileCount(),"player3 should still have all 25 tiles.");
        assertEquals(17, player4.getTotalTileCount(),"player4 should now have 17 tiles.");
        assertInstanceOf(EmptyTile.class, player4.getShipBoard().getTile(0, 4), "player4 should not have a Cannon at (0,4) anymore.");

        // rule 3
        assertEquals(7,player1.getCrewCount(),"player1 should have 7 crew members.");
        assertEquals(7,player2.getCrewCount(),"player2 should have 7 crew members.");
        assertEquals(11,player3.getCrewCount(),"player3 should have 11 crew members.");
        assertEquals(5,player4.getCrewCount(),"player4 should have 5 crew members.");
        // The worst player is player4
        // It removes the single yellow cargo (-3 total value) and 2 batteries.
        assertEquals(1,player1.getShipBoard().getTotalHoldsValue(),"player1 should still have a total holds value of 1.");
        assertEquals(1,player2.getShipBoard().getTotalHoldsValue(),"player2 should still have a total holds value of 1.");
        assertEquals(1,player3.getShipBoard().getTotalHoldsValue(),"player3 should still have a total holds value of 1.");
        assertEquals(0,player4.getShipBoard().getTotalHoldsValue(),"player4 should now have a total holds value of 0.");
        assertEquals(8,player4.getShipBoard().getTotalBatteriesCount(),"player4 should now have 8 batteries.");

        // rule 4
        assertFalse(combatZoneCard.isFinished(), "The card should not be finished.");

        combatZoneCard.activateCannons(player1, 0, 0, new ArrayList<>());
        combatZoneCard.activateCannons(player2, 0, 0, new ArrayList<>());
        combatZoneCard.activateCannons(player4, 0, 0, new ArrayList<>());
        combatZoneCard.activateCannons(player3, 0, 0, new ArrayList<>());
        // The worst player is player3
        // He gives up 3 crew members.
        combatZoneCard.removeMemberFrom(player3, new Point(1, 2));
        combatZoneCard.removeMemberFrom(player3, new Point(1, 2));
        combatZoneCard.removeMemberFrom(player3, new Point(3, 2));
        assertEquals(7,player1.getCrewCount(),"player 1 should still have 6 crew members.");
        assertEquals(7,player2.getCrewCount(),"player 2 should still have 7 crew members.");
        assertEquals(8,player3.getCrewCount(),"player 3 should now have 8 crew members.");
        assertEquals(5,player4.getCrewCount(),"player 4 should still have 5 crew members.");

        assertTrue(combatZoneCard.isFinished(), "The card should be finished.");
    }

    @Test
    void epidemicCard() {
        EpidemicCard epidemicCard = new EpidemicCard(0, 1);

        GameModel gameModel = new GameModel();
        Player player1 = new Player("TestPlayer1", new int[]{0, 0});
        Player player2 = new Player("TestPlayer2", new int[]{0, 0});
        Player player3 = new Player("TestPlayer3", new int[]{0, 0});
        Player player4 = new Player("TestPlayer4", new int[]{0, 0});
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);

        player1.setNewShipBoard(LEVEL_TWO);
        player2.setNewShipBoard(LEVEL_TWO);
        player3.setNewShipBoard(LEVEL_TWO);
        player4.setNewShipBoard(LEVEL_TWO);
        // 1st case
        player1.getShipBoard().setTile(2,2, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);

        // 2nd case
        player2.getShipBoard().setTile(2,2, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);
        player2.getShipBoard().setTile(1,2, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);

        // 3rd  case
        player3.getShipBoard().setTile(3,3, new CabinTile(0,new Connector[]{UNIVERSAL, UNIVERSAL, UNIVERSAL,Connector.NONE}),0);
        player3.getShipBoard().setTile(2,2, new StructuralTile(0,new Connector[]{UNIVERSAL, UNIVERSAL, UNIVERSAL,Connector.NONE}),0);
        player3.getShipBoard().setTile(2,3, new CabinTile(0,new Connector[]{UNIVERSAL,Connector.NONE, UNIVERSAL, UNIVERSAL}),0);

        // 4th case
        player4.getShipBoard().setTile(3,1, new BatteriesTile(0,FULL_UNIVERSAL_CONNECTORS,3),0);
        player4.getShipBoard().setTile(4,1, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);
        player4.getShipBoard().setTile(4,2, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);
        player4.getShipBoard().setTile(4,3, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);
        player4.getShipBoard().setTile(5,3, new CargoTile(0,FULL_UNIVERSAL_CONNECTORS, CargoTile.CargoType.RED,1),0);
        player4.getShipBoard().setTile(6,3, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);

        assertEquals(4, player1.getCrewCount(),"Crew count should be: 4.");
        assertEquals(6, player2.getCrewCount(),"Crew count should be: 6.");
        assertEquals(6, player3.getCrewCount(),"Crew count should be: 6.");
        assertEquals(10, player4.getCrewCount(),"Crew count should be: 10.");

        epidemicCard.activate(gameModel);

        assertEquals(2, player1.getCrewCount(),"Crew count should now be: 2.");
        assertEquals(3, player2.getCrewCount(),"Crew count should now be: 3.");
        assertEquals(4, player3.getCrewCount(),"Crew count should now be: 4.");
        assertEquals(6, player4.getCrewCount(),"Crew count should now be: 6.");

        assertTrue(epidemicCard.isFinished(), "The card should be finished.");
    }

    @Test
    void meteorSwarmCard() {
        Meteor[] meteors = new Meteor[]{
                new Meteor(Projectile.Size.LARGE, Projectile.Source.RIGHT, 5),
                new Meteor(Projectile.Size.SMALL, Projectile.Source.UP, 3),
                new Meteor(Projectile.Size.LARGE, Projectile.Source.LEFT, 4),
                new Meteor(Projectile.Size.LARGE, Projectile.Source.UP, 3)
        };
        MeteorSwarmCard meteorsCard = new MeteorSwarmCard(0, 2, meteors);

        GameModel gameModel = new GameModel();
        Player player1 = new Player("player1", new int[]{0,0}, defaultShipBoard(1));
        Player player2 = new Player("player2", new int[]{0,0}, defaultShipBoard(2));
        Player player3 = new Player("player3", new int[]{0,0}, defaultShipBoard(3));
        Player player4 = new Player("player4", new int[]{0,0}, defaultShipBoard(4));
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);

        player4.getShipBoard().loadCargoAt(1, 3, 0, YELLOW);

        gameModel.setPlayerPosition(player1, 4);
        gameModel.setPlayerPosition(player2, 3);
        gameModel.setPlayerPosition(player3, 2);
        gameModel.setPlayerPosition(player4, 1);

        assertEquals(26, player1.getTotalTileCount(), "player1 should start with 26 tiles.");
        assertEquals(25, player2.getTotalTileCount(), "player2 should start with 25 tiles.");
        assertEquals(25, player3.getTotalTileCount(), "player3 should start with 25 tiles.");
        assertEquals(22, player4.getTotalTileCount(), "player4 should start with 22 tiles.");

        meteorsCard.activate(gameModel);
        // Meteor 1
        // First meteor should miss all the ships.

        // Meteor 2
        // Only player1 is at risk, can use shields to defend himself.
        assertEquals(26, player1.getTotalTileCount(), "player1 should still have all 26 tiles.");
        meteorsCard.useBattery(player1, new Point(6,3));

        // Meteor 3
        // All players are at risk, only player3 can defend himself.
        assertEquals(25, player1.getTotalTileCount(), "player1 should now have 25 tiles.");
        assertEquals(24, player2.getTotalTileCount(), "player2 should now have 24 tiles.");
        assertEquals(21, player4.getTotalTileCount(), "player4 should now have 21 tiles.");
        meteorsCard.useBattery(player3, new Point(1,3));
        assertEquals(25, player3.getTotalTileCount(), "player3 should still have 25 tiles.");

        // Meteor 4
        // player1 can't defend, player2 automatically defend,
        // player3 and player4 need to use a battery.
        assertEquals(24, player1.getTotalTileCount(), "player1 should now have 24 tiles.");
        assertEquals(24, player2.getTotalTileCount(), "player2 should still have 24 tiles.");
        meteorsCard.refuseBattery(player4);
        meteorsCard.useBattery(player3, new Point(1,3));
        assertEquals(25, player3.getTotalTileCount(), "player3 should still have 25 tiles.");
        assertEquals(20, player4.getTotalTileCount(), "player4 should now have 20 tiles.");

        assertTrue(meteorsCard.isFinished(), "The card should be finished.");
    }

    @Test
    void openSpaceCard() {
        OpenSpaceCard openSpaceCard = new OpenSpaceCard(0, 2);

        GameModel gameModel = new GameModel();
        Player player1 = new Player("player1", new int[]{0,0}, defaultShipBoard(1));
        Player player2 = new Player("player2", new int[]{0,0}, defaultShipBoard(2));
        Player player3 = new Player("player3", new int[]{0,0}, defaultShipBoard(3));
        Player player4 = new Player("player4", new int[]{0,0}, defaultShipBoard(4));
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);
        player1.getShipBoard().setCrewTypeAt(2,2,CrewType.BROWN_ALIEN);
        player2.getShipBoard().setCrewTypeAt(1,3,CrewType.BROWN_ALIEN);
        player3.getShipBoard().setCrewTypeAt(6,3,CrewType.BROWN_ALIEN);
        gameModel.setPlayerPosition(player1, 4);
        gameModel.setPlayerPosition(player2, 3);
        gameModel.setPlayerPosition(player3, 2);
        gameModel.setPlayerPosition(player4, 1);

        openSpaceCard.activate(gameModel);

        // decision order: player1, player2, player3, player4
        assertEquals(4, player1.getPosition(),"player1 should start at position 4");
        assertThrows(IllegalStateException.class, () -> openSpaceCard.activateEngines(player2, 0, new ArrayList<>()));
        openSpaceCard.activateEngines(player1, 0, new ArrayList<>());
        assertEquals(11, player1.getPosition(),"player1 should end up at position 11 (Engine strength == 7)");

        assertEquals(3, player2.getPosition(),"player2 should start at position 3");
        assertThrows(IllegalArgumentException.class, () -> openSpaceCard.activateEngines(player2, 1, new ArrayList<>()));
        assertThrows(IllegalArgumentException.class, () -> openSpaceCard.activateEngines(player2, 1, List.of(new Point(2, 3))));
        openSpaceCard.activateEngines(player2, 1, List.of(new Point(5, 3)));
        assertEquals(13, player2.getPosition(),"player2 should end up at position 13 (Engine strength == 9 + 1 player skip)");

        assertEquals(2, player3.getPosition(),"player3 should start at position 2");
        openSpaceCard.activateEngines(player3, 2, List.of(new Point(1, 4), new Point(1, 4)));
        assertEquals(9, player3.getPosition(),"player3 should end up at position 9 (Engine strength == 7)");

        assertEquals(1, player4.getPosition(),"player4 should start at position 1");
        openSpaceCard.activateEngines(player4, 0, new ArrayList<>());
        assertEquals(4, player4.getPosition(),"player4 should end up at position 4 (Engine strength == 3)");

        assertTrue(openSpaceCard.isFinished(), "The card should be finished.");
    }

    @Test
    void piratesCard() {
        CannonShot[] cannonShots = new CannonShot[]{
                new CannonShot(Projectile.Size.LARGE, Projectile.Source.LEFT, 4),
                new CannonShot(Projectile.Size.SMALL, Projectile.Source.UP, 3),
        };
        PiratesCard piratesCard = new PiratesCard(0,2,6,3,3, cannonShots);

        GameModel gameModel = new GameModel();
        Player player1 = new Player("player1", new int[]{0,0}, defaultShipBoard(1));
        Player player2 = new Player("player2", new int[]{0,0}, defaultShipBoard(2));
        Player player3 = new Player("player3", new int[]{0,0}, defaultShipBoard(3));
        Player player4 = new Player("player4", new int[]{0,0}, defaultShipBoard(4));
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);
        player4.getShipBoard().setCrewTypeAt(4,3,CrewType.PURPLE_ALIEN);
        gameModel.setPlayerPosition(player1, 4);
        gameModel.setPlayerPosition(player2, 3);
        gameModel.setPlayerPosition(player3, 2);
        gameModel.setPlayerPosition(player4, 1);

        assertEquals(26, player1.getTotalTileCount(), "player1 should start with 26 tiles.");
        assertEquals(25, player2.getTotalTileCount(), "player2 should start with 25 tiles.");
        assertEquals(25, player3.getTotalTileCount(), "player3 should start with 25 tiles.");
        assertEquals(22, player4.getTotalTileCount(), "player4 should start with 22 tiles.");

        piratesCard.activate(gameModel);

        // player1 cannot achieve cannon power >= 6, gets automatically shot at.
        assertEquals(25, player1.getTotalTileCount(), "player1 should now have 25 tiles.");
        piratesCard.useBattery(player1, new Point(0,3));
        assertEquals(25, player1.getTotalTileCount(), "player1 should still have 25 tiles.");

        // player2 can reach cannon power 6, decides not to.
        piratesCard.activateCannons(player2, 0, 0, new ArrayList<>());
        assertEquals(24, player2.getTotalTileCount(), "player2 should now have 23 tiles.");
        piratesCard.refuseBattery(player2);
        assertEquals(23, player2.getTotalTileCount(), "player2 should now have 23 tiles.");

        // player3 matches cannon power 6, gets ignored.
        piratesCard.activateCannons(player3, 1, 2, List.of(new Point(1,4), new Point(1,4), new Point(1,3)));
        assertEquals(25, player3.getTotalTileCount(), "player3 should still have all 25 tiles.");

        // player4 goes over cannon power 6, and accepts the reward.
        piratesCard.activateCannons(player4, 1, 0, List.of(new Point(2,0)));
        assertEquals(22, player4.getTotalTileCount(), "player4 should still have all 22 tiles.");
        piratesCard.accept(player4);
        assertEquals(3, player4.getCredits(), "player4 should now have 3 credits.");
        assertEquals(-2, player4.getPosition(), "player4 should now be at position -2.");

        assertTrue(piratesCard.isFinished(), "The card should be finished.");
    }

    @Test
    void planetsCard() {
        BlockType[][] blockTypes = {
                {RED, RED, RED, RED},
                {GREEN, GREEN, YELLOW, YELLOW},
                {GREEN, GREEN},
                {BLUE, BLUE}
        };
        PlanetsCard planetsCard = new PlanetsCard(0,2,blockTypes,3);

        GameModel gameModel = new GameModel();
        Player player1 = new Player("player1", new int[]{0,0}, defaultShipBoard(1));
        Player player2 = new Player("player2", new int[]{0,0}, defaultShipBoard(2));
        Player player3 = new Player("player3", new int[]{0,0}, defaultShipBoard(3));
        Player player4 = new Player("player4", new int[]{0,0}, defaultShipBoard(4));
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);

        player4.getShipBoard().loadCargoAt(1, 3, 0, YELLOW);

        gameModel.setPlayerPosition(player1, 4);
        gameModel.setPlayerPosition(player2, 3);
        gameModel.setPlayerPosition(player3, 2);
        gameModel.setPlayerPosition(player4, 1);

        planetsCard.activate(gameModel);

        assertNull(player1.getCargoPool());
        assertNull(player2.getCargoPool());
        assertNull(player3.getCargoPool());
        assertNull(player4.getCargoPool());

        // The decision order is: player1, player2, player3, player4.
        // player1 accepts the first planet.
        planetsCard.choosePlanet(player1, 0);
        // player2 and player3 refuses the reward.
        planetsCard.refusePlanet(player2);
        planetsCard.refusePlanet(player3);
        // player4 accepts the fourth planet.
        assertThrows(IllegalArgumentException.class, () -> planetsCard.choosePlanet(player4, 4));
        planetsCard.choosePlanet(player4, 3);

        assertEquals(0, player1.getCargoHoldsValue());
        assertEquals(0, player2.getCargoHoldsValue());
        assertEquals(0, player3.getCargoHoldsValue());
        assertEquals(3, player4.getCargoHoldsValue());

        assertEquals(List.of(RED, RED, RED, RED), player1.getCargoPool());
        assertNull(player2.getCargoPool());
        assertNull(player3.getCargoPool());
        assertEquals(List.of(BLUE, BLUE), player4.getCargoPool());

        planetsCard.loadCargo(player4, 0, new Point(1, 3), 0);
        assertEquals(List.of(BLUE, YELLOW), player4.getCargoPool());
        assertEquals(1, player4.getCargoHoldsValue());

        planetsCard.loadCargo(player1, 2, new Point(0, 2), 1);
        assertEquals(List.of(RED, RED, RED), player1.getCargoPool());
        assertEquals(4, player1.getCargoHoldsValue());

        planetsCard.confirmDone(player1);
        assertThrows(IllegalStateException.class, () -> planetsCard.loadCargo(player1, 2, new Point(0, 2), 1));
        assertThrows(IllegalStateException.class, () -> planetsCard.confirmDone(player3));

        planetsCard.loadCargo(player4, 1, new Point(1, 3), 1);
        assertEquals(List.of(BLUE), player4.getCargoPool());
        assertEquals(4, player4.getCargoHoldsValue());

        planetsCard.confirmDone(player4);

        assertEquals(4, player1.getCargoHoldsValue());
        assertEquals(0, player2.getCargoHoldsValue());
        assertEquals(0, player3.getCargoHoldsValue());
        assertEquals(4, player4.getCargoHoldsValue());

        assertTrue(planetsCard.isFinished(), "The card should be finished.");
    }

    @Test
    void slaversCard() {
        SlaversCard slaversCard = new SlaversCard(0,2,5,3,3,4);

        GameModel gameModel = new GameModel();
        Player player1 = new Player("player1", new int[]{0,0}, defaultShipBoard(1));
        Player player2 = new Player("player2", new int[]{0,0}, defaultShipBoard(2));
        Player player3 = new Player("player3", new int[]{0,0}, defaultShipBoard(3));
        Player player4 = new Player("player4", new int[]{0,0}, defaultShipBoard(4));
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);
        player4.getShipBoard().setCrewTypeAt(4,3,CrewType.PURPLE_ALIEN);
        gameModel.setPlayerPosition(player1, 4);
        gameModel.setPlayerPosition(player2, 3);
        gameModel.setPlayerPosition(player3, 2);
        gameModel.setPlayerPosition(player4, 1);

        assertEquals(8, player1.getCrewCount(), "player1 should start with 8 crew members.");
        assertEquals(8, player2.getCrewCount(), "player2 should start with 8 crew members.");
        assertEquals(12, player3.getCrewCount(), "player3 should start with 12 crew members.");
        assertEquals(5, player4.getCrewCount(), "player4 should start with 5 crew members.");

        slaversCard.activate(gameModel);

        // player1 matches cannon power 5, gets ignored.
        slaversCard.activateCannons(player1, 1, 0, List.of(new Point(0,3)));
        assertEquals(8, player1.getCrewCount(), "player1 should still have all 8 crew members.");

        // player2 can reach cannon power 5, decides not to.
        slaversCard.activateCannons(player2, 0, 0, new ArrayList<>());
        assertEquals(8, player2.getCrewCount(), "player2 should now have 8 crew members.");
        slaversCard.removeMemberFrom(player2, new Point(1,2));
        slaversCard.removeMemberFrom(player2, new Point(1,2));
        slaversCard.removeMemberFrom(player2, new Point(1,3));
        assertThrows(IllegalArgumentException.class, () -> slaversCard.removeMemberFrom(player2, new Point(1,1)));
        slaversCard.removeMemberFrom(player2, new Point(3,2));
        assertThrows(IllegalStateException.class, () -> slaversCard.removeMemberFrom(player2, new Point(3,2)));
        assertEquals(4, player2.getCrewCount(), "player2 should now have 4 crew members.");

        // player3 goes over cannon power 5, then refuses the reward.
        slaversCard.activateCannons(player3, 1, 2, List.of(new Point(1,4), new Point(1,4), new Point(1,3)));
        assertEquals(12, player3.getCrewCount(), "player2 should still have all 12 crew members.");
        slaversCard.refuse(player3);
        assertEquals(0, player3.getCredits(), "player4 should still have 0 credits.");
        assertEquals(2, player3.getPosition(), "player4 should still be at position 2.");

        // player4 cannot take decisions.
        assertThrows(IllegalStateException.class, () -> slaversCard.activateCannons(player4, 0, 0, new ArrayList<>()));

        assertTrue(slaversCard.isFinished(), "The card should be finished.");
    }

    @Test
    void smugglersCard() {
        BlockType[] rewardCargo = new BlockType[]{GREEN, BLUE, YELLOW};
        SmugglersCard smugglersCard = new SmugglersCard(0,2,4,rewardCargo,3,4);

        GameModel gameModel = new GameModel();
        Player player1 = new Player("player1", new int[]{0,0}, defaultShipBoard(1));
        Player player2 = new Player("player2", new int[]{0,0}, defaultShipBoard(2));
        Player player3 = new Player("player3", new int[]{0,0}, defaultShipBoard(3));
        Player player4 = new Player("player4", new int[]{0,0}, defaultShipBoard(4));
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);
        player1.getShipBoard().loadCargoAt(0, 2, 1, RED);
        player2.getShipBoard().loadCargoAt(4, 3, 1, GREEN);
        player4.getShipBoard().loadCargoAt(1, 3, 0, YELLOW);
        gameModel.setPlayerPosition(player1, 4);
        gameModel.setPlayerPosition(player2, 3);
        gameModel.setPlayerPosition(player3, 2);
        gameModel.setPlayerPosition(player4, 1);

        assertEquals(4, player1.getCargoHoldsValue(), "player1 should start with 1 cargo hold (with value == 4).");
        assertEquals(5, player1.getShipBoard().getTotalBatteriesCount(), "player1 should start with 5 batteries.");
        assertEquals(2, player2.getCargoHoldsValue(), "player2 should start with 1 cargo hold (with value == 2).");
        assertEquals(0, player3.getCargoHoldsValue(), "player3 should start with 0 cargo holds.");
        assertEquals(3, player4.getCargoHoldsValue(), "player4 should start with 1 cargo hold (with value == 3).");

        smugglersCard.activate(gameModel);

        // player1 can reach cannon power 4, decides not to.
        smugglersCard.activateCannons(player1, 0, 0, new ArrayList<>());
        assertEquals(0, player1.getCargoHoldsValue(), "player1 should now have 0 cargo holds");
        assertEquals(2, player1.getShipBoard().getTotalBatteriesCount(), "player1 should now have 2 batteries.");

        // player2 has always cannon power over 4, automatically defeats the smugglers.
        assertThrows(IllegalStateException.class, () -> smugglersCard.activateCannons(player2, 0, 0, new ArrayList<>()));
        smugglersCard.accept(player2);
        assertEquals(List.of(GREEN, BLUE, YELLOW), player2.getCargoPool());
        smugglersCard.loadCargo(player2, 0, new Point(4,3), 1);
        assertEquals(List.of(BLUE, YELLOW, GREEN), player2.getCargoPool());
        smugglersCard.loadCargo(player2, 0, new Point(4,3), 0);
        assertEquals(List.of(YELLOW, GREEN), player2.getCargoPool());
        smugglersCard.loadCargo(player2, 1, new Point(4,1), 0);
        assertEquals(List.of(YELLOW), player2.getCargoPool());
        smugglersCard.loadCargo(player2, 0, new Point(6,3), 1);
        assertEquals(List.of(), player2.getCargoPool());
        smugglersCard.confirmDone(player2);
        assertEquals(8, player2.getCargoHoldsValue(), "player2 should now have 4 cargo hold (with total value == 8).");

        // player3 cannot take decisions.
        assertThrows(IllegalStateException.class, () -> smugglersCard.activateCannons(player4, 0, 0, new ArrayList<>()));

        // player4 cannot take decisions.
        assertThrows(IllegalStateException.class, () -> smugglersCard.refuse(player4));

        assertTrue(smugglersCard.isFinished(), "The card should be finished.");
    }

    @Test
    void starDustCard() {
        StarDustCard starDustCard = new StarDustCard(24,1);

        GameModel gameModel = new GameModel();
        Player player1 = new Player("TestPlayer1", new int[]{0, 0});
        Player player2 = new Player("TestPlayer2", new int[]{0, 0});
        Player player3 = new Player("TestPlayer3", new int[]{0, 0});
        Player player4 = new Player("TestPlayer4", new int[]{0, 0});
        gameModel.addPlayer(player1);
        gameModel.addPlayer(player2);
        gameModel.addPlayer(player3);
        gameModel.addPlayer(player4);

        player1.setNewShipBoard(LEVEL_TWO);
        player2.setNewShipBoard(LEVEL_TWO);
        player3.setNewShipBoard(LEVEL_TWO);
        player4.setNewShipBoard(LEVEL_TWO);

        // 1st case
        player1.getShipBoard().setTile(2,2, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);

        // 2nd case
        player2.getShipBoard().setTile(2,2, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);
        player2.getShipBoard().setTile(1,2, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);

        // 3rd  case
        player3.getShipBoard().setTile(3,3, new CabinTile(0,new Connector[]{UNIVERSAL,Connector.NONE, UNIVERSAL,Connector.NONE}),0);
        player3.getShipBoard().setTile(2,2, new StructuralTile(0,new Connector[]{UNIVERSAL, UNIVERSAL, UNIVERSAL,Connector.NONE}),0);
        player3.getShipBoard().setTile(2,3, new CabinTile(0,new Connector[]{UNIVERSAL,Connector.NONE, UNIVERSAL, UNIVERSAL}),0);

        // 4th case
        player4.getShipBoard().setTile(3,1, new BatteriesTile(0,FULL_UNIVERSAL_CONNECTORS,3),0);
        player4.getShipBoard().setTile(4,1, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);
        player4.getShipBoard().setTile(4,2, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);
        player4.getShipBoard().setTile(4,3, new CabinTile(0,FULL_UNIVERSAL_CONNECTORS),0);
        player4.getShipBoard().setTile(5,3, new CargoTile(0,FULL_UNIVERSAL_CONNECTORS, CargoTile.CargoType.RED,1),0);
        player4.getShipBoard().setTile(6,3, new CabinTile(0,new Connector[]{UNIVERSAL, UNIVERSAL,Connector.NONE, UNIVERSAL}),0);

        gameModel.setPlayerPosition(player1, 10);
        gameModel.setPlayerPosition(player2, 1);
        gameModel.setPlayerPosition(player3, 7);
        gameModel.setPlayerPosition(player4, 5);

        starDustCard.activate(gameModel);

        assertEquals(6, player1.getExposedConnectorsCount(), "player1 should have 6 exposed connectors.");
        assertEquals(8, player2.getExposedConnectorsCount(), "player2 should have 8 exposed connectors.");
        assertEquals(6, player3.getExposedConnectorsCount(), "player3 should have 6 exposed connectors.");
        assertEquals(13, player4.getExposedConnectorsCount(), "player4 should have 13 exposed connectors.");

        assertEquals(4, player1.getPosition(),"player1 should end up in position 2");
        assertEquals(-7, player2.getPosition(),"player2 should end up in position -8");
        assertEquals(1, player3.getPosition(),"player3 should end up in position -2");
        assertEquals(-9, player4.getPosition(),"player4 should end up in position -10");

        assertTrue(starDustCard.isFinished(), "The card should be finished.");
    }
}