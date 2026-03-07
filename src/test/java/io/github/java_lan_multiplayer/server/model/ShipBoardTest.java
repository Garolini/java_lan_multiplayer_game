package io.github.java_lan_multiplayer.server.model;

import io.github.java_lan_multiplayer.server.model.tiles.*;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.Set;

import static io.github.java_lan_multiplayer.server.model.BoardType.*;
import static io.github.java_lan_multiplayer.server.model.cards.projectile.Projectile.*;
import static io.github.java_lan_multiplayer.server.model.tiles.CargoTile.CargoType.*;
import static io.github.java_lan_multiplayer.server.model.tiles.Connector.*;
import static io.github.java_lan_multiplayer.server.model.ShipBoard.*;
import static org.junit.jupiter.api.Assertions.*;

public class ShipBoardTest {

    private static final Connector[] FULL_UNIVERSAL_CONNECTORS = {UNIVERSAL, UNIVERSAL, UNIVERSAL, UNIVERSAL};
    private static final Connector[] FULL_DOUBLE_CONNECTORS = {DOUBLE, DOUBLE, DOUBLE, DOUBLE};
    private static final Connector[] FULL_SINGLE_CONNECTORS = {SINGLE, SINGLE, SINGLE, SINGLE};
    private static final Connector[] FULL_NONE_CONNECTORS = {NONE, NONE, NONE, NONE};

    @Test
    void initializationBoard() {
        ShipBoard ship1 = new ShipBoard(LEARNING);
        Tile[][] tiles1 = ship1.getTiles();
        for (int y = 0; y < tiles1.length; y++) {
            for (int x = 0; x < tiles1[y].length; x++) {
                if (ship1.isInsideShip(x, y)) {
                    if(x == 3 && y == 2) assertInstanceOf(StartingCabinTile.class, tiles1[y][x], "Tile should be StartingCabinTile after initialization.");
                    else assertInstanceOf(EmptyTile.class, tiles1[y][x], "Tile should be EmptyTile after initialization.");
                }
            }
        }
        ShipBoard ship2 = new ShipBoard(LEARNING);
        Tile[][] tiles2 = ship2.getTiles();
        for (int y = 0; y < tiles2.length; y++) {
            for (int x = 0; x < tiles2[y].length; x++) {
                if (ship2.isInsideShip(x, y)) {
                    if(x == 3 && y == 2) assertInstanceOf(StartingCabinTile.class, tiles2[y][x], "Tile should be StartingCabinTile after initialization.");
                    else assertInstanceOf(EmptyTile.class, tiles2[y][x], "Tile should be EmptyTile after initialization.");
                }
            }
        }
    }

    @Test
    void isInsideShip() {
        ShipBoard learningShip = new ShipBoard(LEARNING);
        assertFalse(learningShip.isInsideShip(0, -1), "Coordinate (0,-1) should be outside the learning ship.");
        assertTrue(learningShip.isInsideShip(3, 2), "Coordinate (3,2) should be inside the learning ship.");
        assertFalse(learningShip.isInsideShip(2, 0), "Coordinate (2,0) should be outside the learning ship.");
        assertTrue(learningShip.isInsideShip(3, 0), "Coordinate (3,0) should be inside the learning ship.");
        assertFalse(learningShip.isInsideShip(0, 0), "Coordinate (0,0) should be outside the learning ship.");
        ShipBoard levelTwoShip = new ShipBoard(LEVEL_TWO);
        assertFalse(levelTwoShip.isInsideShip(-1, 0), "Coordinate (-1,0) should be outside the level two ship.");
        assertTrue(levelTwoShip.isInsideShip(3, 2), "Coordinate (3,2) should be inside the level two ship.");
        assertTrue(levelTwoShip.isInsideShip(2, 0), "Coordinate (2,0) should be inside the level two ship.");
        assertFalse(levelTwoShip.isInsideShip(3, 0), "Coordinate (3,0) should be outside the level two ship.");
        assertFalse(levelTwoShip.isInsideShip(0, 0), "Coordinate (0,0) should be outside the level two ship.");
    }

    @Test
    void setTileValid() {
        ShipBoard ship = new ShipBoard(LEARNING);
        CabinTile cabin = new CabinTile(0, FULL_UNIVERSAL_CONNECTORS);
        ship.setTile(2, 2, cabin, 1);
        assertEquals(cabin, ship.getTile(2, 2), "The tile should be the cabin.");
        assertEquals(1, cabin.getRotation(), "The tile rotation should be 1.");
    }

    @Test
    void setTileValidFactory() {
        ShipBoard ship = new ShipBoard(LEARNING);
        ship.setTile(2, 2, 40, 1);
        assertInstanceOf(CabinTile.class, ship.getTile(2, 2), "The tile should be a cabin.");
        assertEquals(SINGLE, ship.getTile(2, 2).getConnector(2), "Given the rotation, the bottom connector should be single.");
        assertThrows(Exception.class, () -> ship.setTile(1, 2, 300, 0), "The factory should throw an exception");
    }

    @Test
    void tileFactory() {
        // Checks if it generates all 152 cards
        TileFactory tileFactory = TileFactory.getInstance();
        for (int id = 0; id < 152; id++) {
            Tile tile = tileFactory.getTile(id);
            assertNotNull(tile, "Tile with ID " + id + " should not be null.");
        }
    }

    @Test
    void setTileOccupied() {
        ShipBoard ship = new ShipBoard(LEARNING);
        ship.setTile(2, 2, new CabinTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> ship.setTile(2, 2, new CabinTile(0, FULL_UNIVERSAL_CONNECTORS), 0));
        assertTrue(exception.getMessage().contains("Tile occupied"), "Exception message should indicate tile occupation.");
    }

    @Test
    void getBatteriesCount() {
        ShipBoard ship = new ShipBoard(LEARNING);
        ship.setTile(2, 2,  new BatteriesTile(0, FULL_UNIVERSAL_CONNECTORS, 3), 0);
        assertEquals(3, ship.getTotalBatteriesCount(), "Batteries count should be 3.");
        ship.setTile(4, 2,  new BatteriesTile(0, FULL_UNIVERSAL_CONNECTORS, 2), 0);
        assertEquals(5, ship.getTotalBatteriesCount(), "Batteries count should be 5.");
    }

    @Test
    void isSideShielded() {
        ShipBoard ship = new ShipBoard(LEARNING);
        ship.setTile(2, 2, new ShieldTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        assertFalse(ship.isSideShielded(0), "The shield should not have energy to activate.");
        ship.setTile(4, 2, new BatteriesTile(0, FULL_UNIVERSAL_CONNECTORS, 3), 0);
        assertTrue(ship.isSideShielded(0), "The shield should be able to shield from the top of the ship.");
        assertTrue(ship.isSideShielded(1), "The shield should be able to shield from the right side of the ship.");
        assertFalse(ship.isSideShielded(2), "The shield should not be able to shield from the bottom of the ship.");
        assertFalse(ship.isSideShielded(3), "The shield should not be able to shield from the left side of the ship.");
    }

    @Test
    void removeCrewMemberAtAt() {
        ShipBoard ship = new ShipBoard(LEARNING);
        CabinTile cabin = new CabinTile(0, FULL_UNIVERSAL_CONNECTORS);
        ship.setTile(2, 2, cabin, 0);
        assertEquals(4, ship.getTotalHumanCount(), "Human count should be 4.");
        ship.removeCrewMemberAt(2, 2);
        assertEquals(3, ship.getTotalHumanCount(), "Human count should be 3.");
        ship.removeCrewMemberAt(2, 2);
        assertEquals(2, ship.getTotalHumanCount(), "Human count should be 2.");
        assertEquals(CrewType.NONE, ship.removeCrewMemberAt(2,2), "Crew member should not exist.");
        ship.removeCrewMemberAt(3, 2);
        assertEquals(1, ship.getTotalHumanCount(), "Human count should be 1.");
    }

    @Test
    void hasAlien() {
        ShipBoard ship = new ShipBoard(LEARNING);
        assertFalse(ship.hasAlien(CrewType.BROWN_ALIEN), "Should not have a brown alien initially.");
        ship.setTile(2, 2, new CabinTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(1, 2, new AlienSupportTile(0, FULL_UNIVERSAL_CONNECTORS, CrewType.BROWN_ALIEN), 0);
        ship.setCrewTypeAt(2,2, CrewType.BROWN_ALIEN);
        assertTrue(ship.hasAlien(CrewType.BROWN_ALIEN), "Should have a brown alien after setting one.");
        ship.setCrewTypeAt(2, 2, CrewType.DOUBLE_HUMAN);
        assertFalse(ship.hasAlien(CrewType.BROWN_ALIEN), "The alien should have been removed.");
    }

    @Test
    void getCannonStrength() {
        ShipBoard ship = new ShipBoard(LEARNING);
        assertEquals(0, ship.getStrength(CannonTile.class, 0, 0), "Total cannon strength should be 0.");
        ship.setTile(3, 1, new AlienSupportTile(0, FULL_UNIVERSAL_CONNECTORS, CrewType.PURPLE_ALIEN), 0);
        ship.setTile(4, 1, new CabinTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setCrewTypeAt(4,1, CrewType.PURPLE_ALIEN);
        assertEquals(0, ship.getStrength(CannonTile.class, 0, 0), "Total cannon strength should be 0.");
        ship.setTile(2, 2, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, false), 0);
        assertEquals(3, ship.getStrength(CannonTile.class, 0, 0), "Total cannon strength should be 3.");
        ship.setTile(4, 2, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, false), 2);
        assertEquals(3.5, ship.getStrength(CannonTile.class, 0, 0), "Total cannon strength should be 3.5.");
        ship.removeTile(4, 1);
        assertEquals(1.5, ship.getStrength(CannonTile.class, 0, 0), "Total cannon strength should be 1.5.");
        assertThrows(IllegalArgumentException.class, () -> ship.getStrength(CannonTile.class, -1, 0), "The number of cannons can't be negative.");
        assertThrows(IllegalArgumentException.class, () -> ship.getStrength(CannonTile.class, 1, 0), "There should be no double cannons.");
        assertThrows(IllegalArgumentException.class, () -> ship.getStrength(CannonTile.class, 0, 1), "There should be no double cannons.");
        ship.setTile(5, 2, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, true), 0);
        assertEquals(1.5, ship.getStrength(CannonTile.class, 0, 0), "Total cannon strength should be 1.5. The cannon has not been activated.");
        assertThrows(IllegalArgumentException.class, () -> ship.getStrength(CannonTile.class, 0, 1), "There should be no double rotated cannons.");
        assertThrows(IllegalArgumentException.class, () -> ship.getStrength(CannonTile.class, 1, 0), "There are no batteries to activate the cannon.");
        ship.setTile(4, 1, new BatteriesTile(0, FULL_UNIVERSAL_CONNECTORS, 3), 0);
        assertEquals(3.5, ship.getStrength(CannonTile.class, 1, 0), "Total cannon strength should be 3.5.");
        assertThrows(IllegalArgumentException.class, () -> ship.getStrength(CannonTile.class, 0, 1), "There should be no double rotated cannons.");
        ship.setTile(5, 3, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, true), 1);
        assertEquals(1.5, ship.getStrength(CannonTile.class, 0, 0), "Total cannon strength should be 1.5.");
        assertEquals(3.5, ship.getStrength(CannonTile.class, 1, 0), "Total cannon strength should be 3.5.");
        assertEquals(2.5, ship.getStrength(CannonTile.class, 0, 1), "Total cannon strength should be 2.5.");
        assertEquals(4.5, ship.getStrength(CannonTile.class, 1, 1), "Total cannon strength should be 4.5.");
        assertThrows(IllegalArgumentException.class, () -> ship.getStrength(CannonTile.class, 1, 2), "There should be only one double rotated cannon.");
        ship.setTile(3, 3, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, true), 2);
        assertEquals(3.5, ship.getStrength(CannonTile.class, 1, 0), "Total cannon strength should be 3.5.");
        assertEquals(2.5, ship.getStrength(CannonTile.class, 0, 1), "Total cannon strength should be 2.5.");
        assertEquals(4.5, ship.getStrength(CannonTile.class, 1, 1), "Total cannon strength should be 4.5.");
        assertEquals(5.5, ship.getStrength(CannonTile.class, 1, 2), "Total cannon strength should be 5.5.");
        ship.setTile(2, 3, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, true), 2);
        assertEquals(4.5, ship.getStrength(CannonTile.class, 0, 3), "Total cannon strength should be 4.5.");
        assertThrows(IllegalArgumentException.class, () -> ship.getStrength(CannonTile.class, 1, 3), "There shouldn't be enough batteries.");
    }

    @Test
    void getMinMaxCannonStrength() {
        ShipBoard ship = new ShipBoard(LEARNING);
        assertEquals(0, ship.getMinStrength(CannonTile.class), "Total minimal cannon strength should be 0.");
        assertEquals(0, ship.getMaxStrength(CannonTile.class), "Total maximum cannon strength should be 0.");
        ship.setTile(3, 1, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, false), 0);
        assertEquals(1, ship.getMinStrength(CannonTile.class), "Total minimal cannon strength should be 1.");
        assertEquals(1, ship.getMaxStrength(CannonTile.class), "Total maximum cannon strength should be 1.");
        ship.setTile(2, 1, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, true), 0);
        assertEquals(1, ship.getMinStrength(CannonTile.class), "Total minimal cannon strength should be 1.");
        assertEquals(1, ship.getMaxStrength(CannonTile.class), "Total maximum cannon strength should be 1.");
        ship.setTile(2, 3, new BatteriesTile(0, FULL_UNIVERSAL_CONNECTORS, 3), 0);
        assertEquals(1, ship.getMinStrength(CannonTile.class), "Total minimal cannon strength should be 1.");
        assertEquals(3, ship.getMaxStrength(CannonTile.class), "Total maximum cannon strength should be 3.");
        ship.setTile(4, 1, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, true), 1);
        assertEquals(1, ship.getMinStrength(CannonTile.class), "Total minimal cannon strength should be 1.");
        assertEquals(4, ship.getMaxStrength(CannonTile.class), "Total maximum cannon strength should be 4.");
    }

    @Test
    void getEngineStrength() {
        ShipBoard ship = new ShipBoard(LEARNING);
        assertEquals(0, ship.getStrength(EngineTile.class, 0), "Total engine strength should be 0.");
        ship.setTile(3, 1, new CabinTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(4, 1, new AlienSupportTile(0, FULL_UNIVERSAL_CONNECTORS, CrewType.BROWN_ALIEN), 0);
        ship.setCrewTypeAt(3,1, CrewType.BROWN_ALIEN);
        assertEquals(0, ship.getStrength(EngineTile.class, 0), "Total engine strength should be 0.");
        ship.setTile(2, 2, new EngineTile(0, FULL_UNIVERSAL_CONNECTORS, false), 0);
        assertEquals(3, ship.getStrength(EngineTile.class, 0), "Total engine strength should be 3.");
        ship.removeTile(3, 1);
        assertEquals(1, ship.getStrength(EngineTile.class, 0), "Total engine strength should be 1.");
        assertThrows(IllegalArgumentException.class, () -> ship.getStrength(CannonTile.class, -1, 0), "The number of engines can't be negative.");
        assertThrows(IllegalArgumentException.class, () -> ship.getStrength(CannonTile.class, 1, 0), "There should be no double engines.");
        assertThrows(IllegalArgumentException.class, () -> ship.getStrength(CannonTile.class, 0, 1), "There should be no double rotated engines.");
    }

    @Test
    void calculateCargoValue() {
        ShipBoard ship = new ShipBoard(LEARNING);
        CargoTile cargo = new CargoTile(0, FULL_UNIVERSAL_CONNECTORS, NORMAL, 3);
        ship.setTile(2, 2, cargo, 0);
        assertEquals(0, ship.getTotalHoldsValue(), "Cargo value should be 0.");
        ship.loadCargoAt(2, 2, 0, BlockType.YELLOW);
        assertEquals(3, ship.getTotalHoldsValue(), "Cargo value should be 3.");
        ship.loadCargoAt(2, 2, 2, BlockType.BLUE);
        assertEquals(4, ship.getTotalHoldsValue(), "Cargo value should be 4.");
        assertEquals(BlockType.YELLOW, ship.unloadCargoAt(2,2,0), "Should return the removed hold.");
    }

    @Test
    void getExposedConnectorsCount() {
        ShipBoard ship = new ShipBoard(LEARNING);
        CannonTile cannon = new CannonTile(0,FULL_SINGLE_CONNECTORS, false);
        CabinTile cabin = new CabinTile(1,FULL_SINGLE_CONNECTORS);
        assertEquals(4, ship.getExposedConnectorsCount(), "Exposed connectors should be 4.");
        ship.setTile(2, 2, cannon, 0);
        assertEquals(6, ship.getExposedConnectorsCount(), "Exposed connectors should be 6.");
        ship.setTile(3, 1, cabin, 0);
        assertEquals(8, ship.getExposedConnectorsCount(), "Exposed connectors should be 8.");
    }

    @Test
    void tileUpdateAlienSupport() {
        ShipBoard ship = new ShipBoard(LEARNING);
        Connector[] connectors = {UNIVERSAL, UNIVERSAL, UNIVERSAL, NONE};
        CabinTile cabin = new CabinTile(0, connectors);
        AlienSupportTile alienSuppNotConnected = new AlienSupportTile(0, FULL_NONE_CONNECTORS, CrewType.BROWN_ALIEN);
        AlienSupportTile alienSuppDifferentType = new AlienSupportTile(0, FULL_UNIVERSAL_CONNECTORS, CrewType.PURPLE_ALIEN);
        AlienSupportTile alienSupp = new AlienSupportTile(0, FULL_UNIVERSAL_CONNECTORS, CrewType.BROWN_ALIEN);
        AlienSupportTile alienSupp2 = new AlienSupportTile(0, FULL_UNIVERSAL_CONNECTORS, CrewType.BROWN_ALIEN);
        ship.removeTile(3, 2);
        ship.setTile(3, 2, cabin, 0);
        assertFalse(((CabinTile)ship.getTile(3,2)).canHostAlienType(CrewType.BROWN_ALIEN), "The cabin should not be able to support aliens yet.");
        ship.setTile(2, 2, alienSuppNotConnected, 0);
        assertFalse(((CabinTile)ship.getTile(3,2)).canHostAlienType(CrewType.BROWN_ALIEN), "The cabin should not be able to support aliens yet.");
        ship.setTile(3, 3, alienSuppDifferentType, 0);
        assertFalse(((CabinTile)ship.getTile(3,2)).canHostAlienType(CrewType.BROWN_ALIEN), "The cabin should not be able to support brown aliens yet.");
        ship.setTile(3, 1, alienSupp, 0);
        assertTrue(((CabinTile)ship.getTile(3,2)).canHostAlienType(CrewType.BROWN_ALIEN), "The cabin should now be able to support aliens.");
        ship.setTile(4, 2, alienSupp2, 0);
        assertTrue(((CabinTile)ship.getTile(3,2)).canHostAlienType(CrewType.BROWN_ALIEN), "The cabin should still be able to support aliens.");
    }

    @Test
    void tileRemoveAlienSupport() {
        ShipBoard ship = new ShipBoard(LEARNING);
        Connector[] connectors = {UNIVERSAL, UNIVERSAL, UNIVERSAL, NONE};
        CabinTile cabin = new CabinTile(0,connectors);
        AlienSupportTile alienSuppToRemove = new AlienSupportTile(0, FULL_UNIVERSAL_CONNECTORS, CrewType.BROWN_ALIEN);
        AlienSupportTile alienSuppNotConnected = new AlienSupportTile(0, FULL_NONE_CONNECTORS, CrewType.BROWN_ALIEN);
        AlienSupportTile alienSuppStillConnected = new AlienSupportTile(0, FULL_UNIVERSAL_CONNECTORS, CrewType.BROWN_ALIEN);
        AlienSupportTile alienSuppDifferentType = new AlienSupportTile(0, FULL_UNIVERSAL_CONNECTORS, CrewType.PURPLE_ALIEN);
        ship.removeTile(3, 2);
        ship.setTile(3, 2, cabin, 0);
        ship.setTile(3, 1, alienSuppToRemove, 0);
        ship.setTile(2, 2, alienSuppNotConnected, 0);
        ship.setTile(4, 2, alienSuppStillConnected, 0);
        ship.setTile(3, 3, alienSuppDifferentType, 0);
        ship.setCrewTypeAt(3, 2, CrewType.BROWN_ALIEN);
        assertTrue(ship.hasAlienAt(3, 2, CrewType.BROWN_ALIEN), "There should be a brown alien inside the cabin.");
        ship.removeTile(3, 1);
        assertTrue(ship.hasAlienAt(3, 2, CrewType.BROWN_ALIEN), "There should still be a brown alien inside the cabin.");
        assertInstanceOf(EmptyTile.class, ship.getTile(3, 1), "The removed tile should have become an EmptyTile.");
        ship.removeTile(4, 2);
        assertFalse(ship.hasAlienAt(3, 2, CrewType.BROWN_ALIEN), "The alien should have been removed, there are no more brown supporting tiles.");
    }

    @Test
    void automaticTileRemoval() {
        ShipBoard ship = new ShipBoard(LEARNING);
        ship.setTile(2, 2, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(1, 2, new AlienSupportTile(0, FULL_UNIVERSAL_CONNECTORS, CrewType.BROWN_ALIEN), 0);
        ship.setTile(4, 2, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(5, 2, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(3, 1, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(3, 0, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(1, 3, new CabinTile(0, FULL_UNIVERSAL_CONNECTORS), 0);

        ship.setCrewTypeAt(1, 3, CrewType.BROWN_ALIEN);
        ship.setTile(5, 3, new CabinTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        assertEquals(4, ship.getTotalHumanCount(), "There should be 4 humans.");
        ship.removeTile(3, 2);
        assertEquals(2, ship.getTotalHumanCount(), "There should be 2 humans.");
        assertInstanceOf(EmptyTile.class, ship.getTile(1, 3), "All the tiles on the left of the ship should be empty.");
    }

    @Test
    void removeBatteryFrom() {
        ShipBoard ship = new ShipBoard(LEARNING);
        ship.setTile(2, 2, new BatteriesTile(0, FULL_UNIVERSAL_CONNECTORS, 3), 0);
        ship.setTile(1, 2, new BatteriesTile(0, FULL_UNIVERSAL_CONNECTORS, 3), 0);
        ship.setTile(4, 2, new BatteriesTile(0, FULL_UNIVERSAL_CONNECTORS, 2), 0);
        ship.setTile(5, 2, new BatteriesTile(0, FULL_UNIVERSAL_CONNECTORS, 2), 0);

        assertEquals(10, ship.getTotalBatteriesCount(), "There should be 10 batteries.");
        assertEquals(3, ship.getBatteriesOf(2, 2), "Tile should have 3 batteries.");
        assertEquals(2, ship.getBatteriesOf(4, 2), "Tile should have 2 batteries.");
        ship.removeTile(4, 2);
        assertEquals(6, ship.getTotalBatteriesCount(), "There should be 6 batteries.");
        assertTrue(ship.removeBatteryFrom(2, 2), "Should be able to remove a battery.");
        assertEquals(5, ship.getTotalBatteriesCount(), "There should be 5 batteries.");
        assertTrue(ship.removeBatteryFrom(2, 2), "Should be able to remove a battery.");
        assertEquals(4, ship.getTotalBatteriesCount(), "There should be 4 batteries.");
        assertTrue(ship.removeBatteryFrom(2, 2), "Should be able to remove a battery.");
        assertEquals(3, ship.getTotalBatteriesCount(), "There should be 3 batteries.");
        assertFalse(ship.removeBatteryFrom(2, 2), "There should be no more batteries to remove.");
    }

    @Test
    void removeMostValuableCargo() {
        ShipBoard ship = new ShipBoard(LEARNING);
        ship.setTile(2, 2, new BatteriesTile(0, FULL_UNIVERSAL_CONNECTORS, 3), 0);
        ship.setTile(1, 2, new CargoTile(0, FULL_UNIVERSAL_CONNECTORS, NORMAL, 3), 0);
        ship.setTile(1, 3, new CargoTile(0, FULL_UNIVERSAL_CONNECTORS, RED, 2), 0);
        ship.loadCargoAt(1, 2, 0, BlockType.YELLOW);   // 3
        ship.loadCargoAt(1, 2, 1, BlockType.GREEN);    // 2
        ship.loadCargoAt(1, 2, 2, BlockType.BLUE);     // 1
        ship.loadCargoAt(1, 3, 0, BlockType.RED);      // 4
        ship.loadCargoAt(1, 3, 1, BlockType.GREEN);    // 2
        assertEquals(12, ship.getTotalHoldsValue(), "The total value should be 12.");
        assertEquals(3, ship.getTotalBatteriesCount(), "There should be 3 batteries.");
        ship.removeMostValuableCargo(1);
        assertEquals(8, ship.getTotalHoldsValue(), "The total value should be 8.");
        assertEquals(3, ship.getTotalBatteriesCount(), "There should be 3 batteries.");
        ship.removeMostValuableCargo(2);
        assertEquals(3, ship.getTotalHoldsValue(), "The total value should be 3.");
        assertEquals(3, ship.getTotalBatteriesCount(), "There should be 3 batteries.");
        ship.removeMostValuableCargo(4);
        assertEquals(0, ship.getTotalHoldsValue(), "The total value should be 0.");
        assertEquals(1, ship.getTotalBatteriesCount(), "There should be 1 battery.");
    }

    @Test
    void hasTileOnPath() {
        ShipBoard ship = new ShipBoard(LEARNING);
        ship.setTile(2, 2, new CabinTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(1, 2, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, false), 0);
        ship.setTile(3, 3, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, true), 2);
        ship.setTile(2, 3, new CargoTile(0, FULL_UNIVERSAL_CONNECTORS, NORMAL, 3), 0);
        ship.setTile(1, 3, new CargoTile(0, FULL_UNIVERSAL_CONNECTORS, NORMAL, 3), 0);
        assertFalse(ship.hasTileOnPath(Source.UP, 1, CabinTile.class, tile -> true));
        assertTrue(ship.hasTileOnPath(Source.UP, 2, CabinTile.class, tile -> true));
        assertTrue(ship.hasTileOnPath(Source.DOWN, 2, CabinTile.class, tile -> true));
        assertFalse(ship.hasTileOnPath(Source.UP, 2, CabinTile.class, tile -> ((CabinTile)tile).hasAlien(CrewType.BROWN_ALIEN)));
        assertTrue(ship.hasTileOnPath(Source.LEFT, 2, CannonTile.class, tile -> true));
        assertTrue(ship.hasTileOnPath(Source.RIGHT, 2, CannonTile.class, tile -> true));
        assertFalse(ship.hasTileOnPath(Source.LEFT, 2, CannonTile.class, tile -> ((CannonTile)tile).isDouble()));
        assertFalse(ship.hasTileOnPath(Source.RIGHT, 2, CannonTile.class, tile -> ((CannonTile)tile).isDouble()));
        assertFalse(ship.hasTileOnPath(Source.LEFT, 3, CargoTile.class, tile -> ((CargoTile)tile).getHoldsValue() == 3));
        ship.loadCargoAt(2, 3, 0, BlockType.YELLOW);
        assertTrue(ship.hasTileOnPath(Source.LEFT, 3, CargoTile.class, tile -> ((CargoTile)tile).getHoldsValue() == 3));
    }

    @Test
    void isPathEmpty() {
        ShipBoard ship = new ShipBoard(LEARNING);
        ship.setTile(2, 2, new CabinTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(1, 2, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, false), 0);
        ship.setTile(3, 3, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, true), 2);
        ship.setTile(2, 3, new CargoTile(0, FULL_UNIVERSAL_CONNECTORS, NORMAL, 3), 0);
        ship.setTile(1, 3, new CargoTile(0, FULL_UNIVERSAL_CONNECTORS, NORMAL, 3), 0);
        assertTrue(ship.isPathEmpty(Source.LEFT, -1), "Path should be empty.");
        assertTrue(ship.isPathEmpty(Source.RIGHT, 1), "Path should be empty.");
        assertFalse(ship.isPathEmpty(Source.LEFT, 2), "Path should not be empty.");
        assertFalse(ship.isPathEmpty(Source.RIGHT, 3), "Path should not be empty.");
        assertTrue(ship.isPathEmpty(Source.LEFT, 4), "Path should be empty.");
        assertTrue(ship.isPathEmpty(Source.RIGHT, 12), "Path should be empty.");
        assertTrue(ship.isPathEmpty(Source.UP, -1), "Path should be empty.");
        assertFalse(ship.isPathEmpty(Source.DOWN, 1), "Path should not be empty.");
        assertFalse(ship.isPathEmpty(Source.UP, 2), "Path should not be empty.");
        assertFalse(ship.isPathEmpty(Source.DOWN, 3), "Path should not be empty.");
        assertTrue(ship.isPathEmpty(Source.UP, 4), "Path should be empty.");
        assertTrue(ship.isPathEmpty(Source.DOWN, 12), "Path should be empty.");
    }

    @Test
    void getDefendingCannonType() {
        ShipBoard ship = new ShipBoard(LEARNING);
        ship.setTile(2, 2, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, false), 0);
        ship.setTile(4, 2, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, true), 0);
        ship.setTile(2, 3, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, false), 3);
        ship.setTile(4, 3, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, true), 1);
        ship.setTile(4, 4, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, false), 1);
        ship.setTile(3, 3, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, false), 2);
        ship.setTile(2, 4, new BatteriesTile(0, FULL_UNIVERSAL_CONNECTORS, 3), 0);
        assertEquals(DefendingCannonType.NO_CANNON, ship.getDefendingCannonType(Source.UP, -1), "Row should not be defended.");
        assertEquals(DefendingCannonType.NO_CANNON, ship.getDefendingCannonType(Source.UP, 1), "Row should not be defended.");
        assertEquals(DefendingCannonType.SINGLE, ship.getDefendingCannonType(Source.UP, 2), "Row should be defended by a cannon (single)");
        assertEquals(DefendingCannonType.NO_CANNON, ship.getDefendingCannonType(Source.UP, 3), "Row should not be defended.");
        assertEquals(DefendingCannonType.DOUBLE, ship.getDefendingCannonType(Source.UP, 4), "Row should be defended by a cannon (double)");
        assertEquals(DefendingCannonType.NO_CANNON, ship.getDefendingCannonType(Source.UP, 5), "Row should not be defended.");
        assertEquals(DefendingCannonType.NO_CANNON, ship.getDefendingCannonType(Source.LEFT, 1), "Column should not be defended.");
        assertEquals(DefendingCannonType.SINGLE, ship.getDefendingCannonType(Source.LEFT, 2), "Column should be defended by a cannon (single)");
        assertEquals(DefendingCannonType.SINGLE, ship.getDefendingCannonType(Source.LEFT, 3), "Column should be defended by a cannon (single)");
        assertEquals(DefendingCannonType.SINGLE, ship.getDefendingCannonType(Source.LEFT, 4), "Column should be defended by a cannon (single)");
        assertEquals(DefendingCannonType.NO_CANNON, ship.getDefendingCannonType(Source.LEFT, 5), "Column should not be defended.");
        assertEquals(DefendingCannonType.NO_CANNON, ship.getDefendingCannonType(Source.RIGHT, 1), "Column should not be defended.");
        assertEquals(DefendingCannonType.DOUBLE, ship.getDefendingCannonType(Source.RIGHT, 2), "Column should be defended by a cannon (double)");
        assertEquals(DefendingCannonType.SINGLE, ship.getDefendingCannonType(Source.RIGHT, 3), "Method should prioritize the single cannon");
        assertEquals(DefendingCannonType.SINGLE, ship.getDefendingCannonType(Source.RIGHT, 4), "Column should prioritize the single cannon");
        assertEquals(DefendingCannonType.SINGLE, ship.getDefendingCannonType(Source.RIGHT, 5), "Column should be defended by a cannon (single)");
        assertEquals(DefendingCannonType.NO_CANNON, ship.getDefendingCannonType(Source.RIGHT, 6), "Column should not be defended.");
        assertEquals(DefendingCannonType.NO_CANNON, ship.getDefendingCannonType(Source.DOWN, 1), "Row should not be defended.");
        assertEquals(DefendingCannonType.SINGLE, ship.getDefendingCannonType(Source.DOWN, 2), "Row should be defended by a cannon (single)");
        assertEquals(DefendingCannonType.SINGLE, ship.getDefendingCannonType(Source.DOWN, 3), "Row should be defended by a cannon (single)");
        assertEquals(DefendingCannonType.SINGLE, ship.getDefendingCannonType(Source.DOWN, 4), "Row should be defended by a cannon (single)");
        assertEquals(DefendingCannonType.NO_CANNON, ship.getDefendingCannonType(Source.DOWN, 5), "Row should not be defended.");
    }

    @Test
    void removeFirstTileInPath() {
        ShipBoard ship = new ShipBoard(LEVEL_TWO);
        ship.setTile(2, 2, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(1, 2, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(0, 2, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(3, 3, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(2, 3, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(1, 3, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(0, 3, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(2, 4, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(1, 4, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(0, 4, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        assertNull(ship.removeFirstTileInPath(Source.UP, -1));
        assertNull(ship.removeFirstTileInPath(Source.UP, 4));
        assertInstanceOf(StructuralTile.class, ship.getTile(0, 2), "Tile in (0,2) should be a Structural Tile.");
        assertEquals(new Point(0, 2), ship.removeFirstTileInPath(Source.UP, 0));
        assertInstanceOf(EmptyTile.class, ship.getTile(0, 2), "Tile in (0,2) should have been removed.");
        assertEquals(new Point(0, 4), ship.removeFirstTileInPath(Source.DOWN, 0));
        assertEquals(new Point(0, 3), ship.removeFirstTileInPath(Source.UP, 0));
        assertNull(ship.removeFirstTileInPath(Source.DOWN, 0));
        assertEquals(new Point(1, 4), ship.removeFirstTileInPath(Source.LEFT, 4));
        assertEquals(new Point(2, 4), ship.removeFirstTileInPath(Source.RIGHT, 4));
        assertNull(ship.removeFirstTileInPath(Source.LEFT, 4));
    }

    @Test
    void hasExposedConnectorInPath() {
        ShipBoard ship = new ShipBoard(LEVEL_TWO);
        ship.setTile(2, 2, new StructuralTile(0, new Connector[]{DOUBLE, SINGLE, UNIVERSAL, NONE}), 0);
        ship.setTile(2, 3, new StructuralTile(0, new Connector[]{UNIVERSAL, UNIVERSAL, NONE, SINGLE}), 0);
        ship.setTile(4, 2, new StructuralTile(0, new Connector[]{NONE, UNIVERSAL, UNIVERSAL, SINGLE}), 0);
        ship.setTile(4, 3, new StructuralTile(0, new Connector[]{UNIVERSAL, NONE, UNIVERSAL, NONE}), 0);
        assertFalse(ship.hasExposedConnectorInPath(Source.UP, -1));
        assertFalse(ship.hasExposedConnectorInPath(Source.UP, 0));
        assertFalse(ship.hasExposedConnectorInPath(Source.UP, 1));
        assertTrue(ship.hasExposedConnectorInPath(Source.UP, 2));
        assertTrue(ship.hasExposedConnectorInPath(Source.UP, 3));
        ship.setTile(3, 1, new CannonTile(0, new Connector[]{NONE, NONE, UNIVERSAL, DOUBLE}, false), 0);
        assertFalse(ship.hasExposedConnectorInPath(Source.UP, 3));
        assertFalse(ship.hasExposedConnectorInPath(Source.UP, 4));
        assertFalse(ship.hasExposedConnectorInPath(Source.LEFT, -1));
        assertFalse(ship.hasExposedConnectorInPath(Source.LEFT, 0));
        assertTrue(ship.hasExposedConnectorInPath(Source.LEFT, 1));
        assertFalse(ship.hasExposedConnectorInPath(Source.LEFT, 2));
        assertTrue(ship.hasExposedConnectorInPath(Source.LEFT, 3));
        assertFalse(ship.hasExposedConnectorInPath(Source.LEFT, 4));
        assertFalse(ship.hasExposedConnectorInPath(Source.RIGHT, 1));
        assertTrue(ship.hasExposedConnectorInPath(Source.RIGHT, 2));
        assertFalse(ship.hasExposedConnectorInPath(Source.RIGHT, 3));
        assertFalse(ship.hasExposedConnectorInPath(Source.RIGHT, 4));
        assertFalse(ship.hasExposedConnectorInPath(Source.DOWN, 1));
        assertFalse(ship.hasExposedConnectorInPath(Source.DOWN, 2));
        assertTrue(ship.hasExposedConnectorInPath(Source.DOWN, 3));
        assertTrue(ship.hasExposedConnectorInPath(Source.DOWN, 4));
        assertFalse(ship.hasExposedConnectorInPath(Source.DOWN, 5));
    }

    @Test
    void getInvalidTiles() {
        ShipBoard ship = new ShipBoard(LEARNING);
        ship.setTile(2, 2, new CabinTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(3, 1, new BatteriesTile(0, FULL_UNIVERSAL_CONNECTORS, 3), 0);
        ship.setTile(4, 2, new StructuralTile(0, FULL_UNIVERSAL_CONNECTORS), 0);
        ship.setTile(2, 1, new EngineTile(0, FULL_UNIVERSAL_CONNECTORS, false), 0);             // faces into another tile
        ship.setTile(5, 2, new EngineTile(0, FULL_UNIVERSAL_CONNECTORS, false), 1);             // is rotated
        ship.setTile(3, 0, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, false), 0);
        ship.setTile(3, 3, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, false), 0);             // faces into another tile
        ship.setTile(4, 3, new CannonTile(0, FULL_UNIVERSAL_CONNECTORS, false), 2);             // faces into another tile
        ship.setTile(4, 4, new StructuralTile(0, FULL_DOUBLE_CONNECTORS), 0);
        ship.setTile(5, 4, new StructuralTile(0, FULL_SINGLE_CONNECTORS), 0);                           // has non compatible connectors
        ship.setTile(2, 3, new StructuralTile(0, new Connector[]{DOUBLE, UNIVERSAL, NONE, SINGLE}), 0);
        ship.setTile(1, 3, new StructuralTile(0, new Connector[]{NONE, SINGLE, NONE, NONE}), 0);
        ship.setTile(1, 4, new StructuralTile(0, new Connector[]{NONE, NONE, UNIVERSAL, NONE}), 0);     // not connected, but valid

        Set<Point> invalidPoints = Set.of(new Point(2,1), new Point(2,2), new Point(3,2), new Point(5,2),
                new Point(3,3), new Point(4,3), new Point(4,4), new Point(5,4));
        assertEquals(invalidPoints, ship.getInvalidTiles(), "Invalid tiles should match.");
    }
}