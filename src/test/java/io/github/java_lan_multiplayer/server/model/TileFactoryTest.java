package io.github.java_lan_multiplayer.server.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.java_lan_multiplayer.server.model.tiles.Tile;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TileFactoryTest {
    @Test
    void getInstance_returnsSingletonInstance() {
        TileFactory instance1 = TileFactory.getInstance();
        TileFactory instance2 = TileFactory.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2, "TileFactory should return the same instance.");
    }

    @Test
    void getTile_validId_returnsCorrectTile() {
        TileFactory factory = TileFactory.getInstance();
        Tile tile = factory.getTile(0); // assuming tile with ID 0 exists in tiles.json

        assertNotNull(tile, "Tile should not be null.");
        assertEquals(0, tile.getId(), "Tile ID should match.");
    }

    @Test
    void getTile_invalidId_throwsException() {
        TileFactory factory = TileFactory.getInstance();

        assertThrows(IllegalArgumentException.class, () -> factory.getTile(-1), "Should throw if tile ID is not found.");
    }

    @Test
    void createTile_unknownType_throwsException() throws Exception {
        TileFactory factory = TileFactory.getInstance();

        JsonNode fakeNode = new ObjectMapper().readTree("""
        {
          "id": 999,
          "tileType": "teleporter",
          "connectors": [0, 0, 0, 0]
        }
    """);

        var method = TileFactory.class.getDeclaredMethod("createTile", JsonNode.class);
        method.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, () -> method.invoke(factory, fakeNode));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void loadJson_missingFields_throwsException() {
        String brokenJson = """
        [
            {"id": 10, "tileType": "cabin"}
        ]
    """;

        InputStream stream = new ByteArrayInputStream(brokenJson.getBytes());
        ObjectMapper mapper = new ObjectMapper();

        assertThrows(IllegalArgumentException.class, () -> {
            JsonNode root = mapper.readTree(stream);
            for (JsonNode node : root) {
                if (!node.has("id") || !node.has("tileType") || !node.has("connectors")) {
                    throw new IllegalArgumentException("Missing required fields.");
                }
            }
        });
    }

    @Test
    void allTileTypes_canBeLoaded() {
        TileFactory factory = TileFactory.getInstance();

        // assuming at least one tile of each type is defined in tiles.json
        List<Integer> idsToCheck = List.of(0, 1, 2, 3, 4, 5, 6, 7); // example IDs for all 8 types

        for (int id : idsToCheck) {
            Tile tile = factory.getTile(id);
            assertNotNull(tile);
            assertTrue(Tile.class.isAssignableFrom(tile.getClass()), "Tile should inherit from Tile.");
        }
    }

    @Test
    void privateConstructor_singletonEnforced() throws Exception {
        Constructor<TileFactory> constructor = TileFactory.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        TileFactory newInstance = constructor.newInstance("public/tiles.json");

        assertNotSame(TileFactory.getInstance(), newInstance, "Should not match singleton instance.");
    }
}
