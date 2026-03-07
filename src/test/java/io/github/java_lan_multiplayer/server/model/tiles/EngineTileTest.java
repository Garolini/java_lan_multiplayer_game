package io.github.java_lan_multiplayer.server.model.tiles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineTileTest {

    @Test
    void constructorIsDoubleTrueAndFalse() {
        Connector[] connectors = new Connector[4];
        EngineTile singleEngine = new EngineTile(1, connectors, false);
        EngineTile doubleEngine = new EngineTile(2, connectors, true);
        assertFalse(singleEngine.isDouble());
        assertTrue(doubleEngine.isDouble());
    }

    @Test
    void getStrengthRotationZeroIsOneOrTwo() {
        assertEquals(1.0, EngineTile.getStrength(0, false));
        assertEquals(2.0, EngineTile.getStrength(0, true));
    }

    @Test
    void getStrengthRotationNonZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> EngineTile.getStrength(1, false));
        assertThrows(IllegalArgumentException.class, () -> EngineTile.getStrength(2, true));
    }

    @Test
    void getTileTypeReturnsEngineOrDoubleEngine() {
        Connector[] connectors = new Connector[4];
        EngineTile singleEngine = new EngineTile(1, connectors, false);
        EngineTile doubleEngine = new EngineTile(2, connectors, true);
        assertEquals("Engine", singleEngine.getTileType());
        assertEquals("Double Engine", doubleEngine.getTileType());
    }
}