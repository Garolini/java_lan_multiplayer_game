package io.github.java_lan_multiplayer.server.model.tiles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BatteriesTileTest {

    @Test
    void constructor_validCapacity_setsFieldsCorrectly() {
        Connector[] connectors = new Connector[4];
        BatteriesTile tile = new BatteriesTile(1, connectors, 2);

        assertEquals(2, tile.getCapacity());
        assertEquals(2, tile.getStoredBatteries());
        assertTrue(tile.isFull());
    }

    @Test
    void constructor_invalidLowCapacity_throwsException() {
        Connector[] connectors = new Connector[4];
        assertThrows(IllegalArgumentException.class, () -> new BatteriesTile(1, connectors, 1));
    }

    @Test
    void constructor_invalidHighCapacity_throwsException() {
        Connector[] connectors = new Connector[4];
        assertThrows(IllegalArgumentException.class, () -> new BatteriesTile(1, connectors, 4));
    }

    @Test
    void removeBattery_valid_decreasesStoredBattery() {
        BatteriesTile tile = new BatteriesTile(1, new Connector[4], 3);

        assertTrue(tile.removeBattery());
        assertEquals(2, tile.getStoredBatteries());
    }

    @Test
    void removeBattery_empty_returnsFalse() {
        BatteriesTile tile = new BatteriesTile(1, new Connector[4], 2);
        tile.removeBattery();
        tile.removeBattery();

        assertFalse(tile.removeBattery());
        assertEquals(0, tile.getStoredBatteries());
    }

    @Test
    void isFull_returnsCorrectValue() {
        BatteriesTile tile = new BatteriesTile(1, new Connector[4], 2);
        assertTrue(tile.isFull());

        tile.removeBattery();
        assertFalse(tile.isFull());
    }

    @Test
    void getTileType_returnsCorrectString() {
        BatteriesTile tile = new BatteriesTile(1, new Connector[4], 2);
        assertEquals("Battery Container", tile.getTileType());
    }
}
