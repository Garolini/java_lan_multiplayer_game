package io.github.java_lan_multiplayer.server.model.tiles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CannonTileTest {

    @Test
    void constructorSetsIsDoubleCorrectly() {
        Connector[] connectors = new Connector[4];

        CannonTile singleCannon = new CannonTile(1, connectors, false);
        CannonTile doubleCannon = new CannonTile(2, connectors, true);

        assertAll(
                () -> assertFalse(singleCannon.isDouble()),
                () -> assertTrue(doubleCannon.isDouble())
        );
    }

    @Test
    void getStrengthReturnsCorrectValuesForSingleAndDouble() {
        assertAll(
                () -> assertEquals(1.0, CannonTile.getStrength(0, false)),
                () -> assertEquals(0.5, CannonTile.getStrength(1, false)),
                () -> assertEquals(2.0, CannonTile.getStrength(0, true)),
                () -> assertEquals(1.0, CannonTile.getStrength(1, true))
        );
    }

    @Test
    void getTileTypeReturnsCorrectNameBasedOnIsDouble() {
        Connector[] connectors = new Connector[4];

        CannonTile singleCannon = new CannonTile(1, connectors, false);
        CannonTile doubleCannon = new CannonTile(2, connectors, true);

        assertAll(
                () -> assertEquals("Cannon", singleCannon.getTileType()),
                () -> assertEquals("Double Cannon", doubleCannon.getTileType())
        );
    }
}