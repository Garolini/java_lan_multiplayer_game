package io.github.java_lan_multiplayer.server.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.java_lan_multiplayer.server.model.tiles.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Singleton class responsible for loading tile definitions from a JSON file
 * and creating tile instances on demand.
 * <p>
 * The factory parses a JSON file containing tile definitions, maps tile types
 * to concrete {@link Tile} subclasses, and provides methods for retrieving individual tiles by ID.
 * <p>
 * Expected structure of each tile in JSON:
 * [
 *   {
 *     "id": 1,
 *     "tileType": "engine",
 *     "connectors": { ... }
 *   },
 *   ...
 * ]
 */
public class TileFactory {

    private static TileFactory instance;
    private final List<JsonNode> tilesJson;
    private final ObjectMapper mapper;

    /**
     * Private constructor used for the singleton pattern.
     * Loads and parses tile definitions from the provided JSON file path.
     *
     * @param jsonFilePath The path to the tile JSON file.
     */
    private TileFactory(String jsonFilePath) {
        this.mapper = new ObjectMapper();
        this.tilesJson = loadJson(jsonFilePath);
    }

    /**
     * Returns the singleton instance of {@code TileFactory}, initializing it if needed.
     * Defaults to using the path {@code "public/tiles.json"} for tile definitions.
     *
     * @return The singleton instance of {@code TileFactory}.
     */
    public static synchronized TileFactory getInstance() {
        // Retrieves the singleton instance of TileFactory.
        if(instance == null) {
            instance = new TileFactory("public/tiles.json");
        }
        return instance;
    }

    /**
     * Loads tile definitions from the specified JSON file.
     *
     * @param path The file path of the JSON tile definitions.
     * @return A list of {@link JsonNode} objects representing individual tiles.
     * @throws RuntimeException If the file is not found or cannot be parsed.
     * @throws IllegalArgumentException If a tile is missing required fields.
     */
    private List<JsonNode> loadJson(String path) {
        // Loads the JSON file and returns a list of tile definitions.
        List<JsonNode> tileList = new ArrayList<>();
        try{
            InputStream is = TileFactory.class.getClassLoader().getResourceAsStream(path);
            if(is == null) {
                throw new IOException("Could not find " + path);
            }
            JsonNode root = mapper.readTree(is);
            if(!root.isArray()) {
                throw new IllegalArgumentException("Root element must be a JSON array.");
            }
            for (JsonNode tileNode : root) {
                if (!tileNode.has("id") || !tileNode.has("tileType") || !tileNode.has("connectors")) {
                    throw new IllegalArgumentException("Tile is missing required fields: " + tileNode);
                }
                tileList.add(tileNode);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading JSON file at " + path, e);
        }
        return tileList;
    }

    /**
     * Retrieves a {@link Tile} by its unique ID.
     *
     * @param id The ID of the tile to retrieve.
     * @return A fully constructed {@link Tile} object.
     * @throws IllegalArgumentException If no tile with the specified ID exists.
     */
    public Tile getTile(int id) {
        for(JsonNode tileNode : tilesJson) {
            if(tileNode.get("id").asInt() == id) {
                return createTile(tileNode);
            }
        }
        throw new IllegalArgumentException("Tile with ID " + id + " not found.");
    }

    private static final Map<String, Class<? extends Tile>> TILE_MAP = new HashMap<>();
    static {
        TILE_MAP.put("alienSupport", AlienSupportTile.class);
        TILE_MAP.put("batteries", BatteriesTile.class);
        TILE_MAP.put("cabin", CabinTile.class);
        TILE_MAP.put("cannon", CannonTile.class);
        TILE_MAP.put("cargo", CargoTile.class);
        TILE_MAP.put("engine", EngineTile.class);
        TILE_MAP.put("shield", ShieldTile.class);
        TILE_MAP.put("structural", StructuralTile.class);
    }

    /**
     * Instantiates a {@link Tile} based on the given JSON node and its {@code tileType}.
     *
     * @param tileNode The JSON node containing the tile definition.
     * @return A new instance of a subclass of {@code Tile}.
     * @throws IllegalArgumentException If the tile type is unknown.
     * @throws RuntimeException If the tile cannot be deserialized properly.
     */
    private Tile createTile(JsonNode tileNode) {

        String tileType = tileNode.get("tileType").asText();

        Class<? extends Tile> tileClass = TILE_MAP.get(tileType);
        if (tileClass == null) {
            throw new IllegalArgumentException("Unknown type in tile " + tileNode.get("id") + ": " + tileType);
        }
        try {
            return mapper.readValue(tileNode.toString(), tileClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}