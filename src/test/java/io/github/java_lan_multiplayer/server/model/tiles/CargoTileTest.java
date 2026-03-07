package io.github.java_lan_multiplayer.server.model.tiles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CargoTileTest {

    private CargoTile normalCargo;
    private CargoTile redCargo;

    @BeforeEach
    void setUp() {
        normalCargo = new CargoTile(1, new Connector[4], CargoTile.CargoType.NORMAL, 3);
        redCargo = new CargoTile(2, new Connector[4], CargoTile.CargoType.RED, 2);
    }

    @Test
    void constructor_validNormalCargo_createsInstance() {
        assertEquals(CargoTile.CargoType.NORMAL, normalCargo.getType());
        assertEquals(3, normalCargo.getCapacity());
        assertNotNull(normalCargo.getContent());
        assertEquals(3, normalCargo.getContent().length);
    }

    @Test
    void constructor_invalidNormalCapacity_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new CargoTile(3, new Connector[4], CargoTile.CargoType.NORMAL, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new CargoTile(3, new Connector[4], CargoTile.CargoType.NORMAL, 4));
    }

    @Test
    void constructor_validRedCargo_createsInstance() {
        assertEquals(CargoTile.CargoType.RED, redCargo.getType());
        assertEquals(2, redCargo.getCapacity());
        assertNotNull(redCargo.getContent());
        assertEquals(2, redCargo.getContent().length);
    }

    @Test
    void constructor_invalidRedCapacity_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new CargoTile(4, new Connector[4], CargoTile.CargoType.RED, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new CargoTile(4, new Connector[4], CargoTile.CargoType.RED, 3));
    }

    @Test
    void getBlockAt_validIndex_returnsNullInitially() {
        for (int i = 0; i < normalCargo.getCapacity(); i++) {
            assertNull(normalCargo.getBlockAt(i));
        }
    }

    @Test
    void getBlockAt_invalidIndex_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> normalCargo.getBlockAt(-1));
        assertThrows(IllegalArgumentException.class, () -> normalCargo.getBlockAt(normalCargo.getCapacity()));
    }

    @Test
    void loadBlock_invalidIndex_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> normalCargo.loadBlock(-1, BlockType.BLUE));
        assertThrows(IllegalArgumentException.class, () -> normalCargo.loadBlock(normalCargo.getCapacity(), BlockType.BLUE));
    }

    @Test
    void unloadBlock_removesBlockAndReturnsPrevious() {
        normalCargo.loadBlock(0, BlockType.BLUE);
        BlockType removed = normalCargo.unloadBlock(0);
        assertEquals(BlockType.BLUE, removed);
        assertNull(normalCargo.getBlockAt(0));
    }

    @Test
    void unloadBlock_invalidIndex_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> normalCargo.unloadBlock(-1));
        assertThrows(IllegalArgumentException.class, () -> normalCargo.unloadBlock(normalCargo.getCapacity()));
    }

    @Test
    void getHoldsValue_sumsCorrectly() {
        // Initially empty
        assertEquals(0, normalCargo.getHoldsValue());

        normalCargo.loadBlock(0, BlockType.BLUE);   // Suppose value = 1
        normalCargo.loadBlock(1, BlockType.YELLOW);    // Red block value


        int expected = BlockType.BLUE.getValue() + BlockType.YELLOW.getValue();
        assertEquals(expected, normalCargo.getHoldsValue());
    }

    @Test
    void getContent_returnsArrayReference() {
        BlockType[] content = normalCargo.getContent();
        assertNotNull(content);
        assertEquals(normalCargo.getCapacity(), content.length);
    }

    @Test
    void getTileType_returnsCargoHold() {
        assertEquals("Cargo Hold", normalCargo.getTileType());
    }
}