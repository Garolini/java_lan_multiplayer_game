package io.github.java_lan_multiplayer.server.model.tiles;

/**
 * Abstract class representing a tile in the game.
 * Each tile has a fixed ID, an array of four {@link Connector}s representing
 * connections on its sides (in default orientation), and a current rotation state.
 *
 * <p>The actual connector logic under rotation is handled here; visual rotation is done in JavaFX.</p>
 */
public abstract class Tile {

    private final int id;
    private final Connector[] connectors;
    private int rotation; // Rotation: 0 to 3 (clockwise, in 90° steps)

    /**
     * Constructs a tile with the given ID and connectors.
     *
     * @param id         the unique identifier of the tile
     * @param connectors an array of 4 {@link Connector}s representing the sides (top, right, bottom, left)
     * @throws IllegalArgumentException if {@code connectors} is null or its length is not 4
     */
    public Tile(int id, Connector[] connectors) {
        if (connectors == null || connectors.length != 4) {
            throw new IllegalArgumentException("Connectors array must contain exactly 4 elements.");
        }
        this.id = id;
        this.connectors = connectors;
        this.rotation = 0;
    }

    /**
     * Returns the unique ID of the tile.
     *
     * @return the tile ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the current rotation state of the tile.
     * Values are 0 (0°), 1 (90°), 2 (180°), 3 (270°).
     *
     * @return the rotation of the tile
     */
    public int getRotation() {
        return rotation;
    }

    /**
     * Sets the rotation of the tile.
     * Rotation must be between 0 and 3 (inclusive).
     *
     * @param rotation the new rotation value (0–3)
     * @throws IllegalArgumentException if rotation is outside the allowed range
     */
    public void setRotation(int rotation) {
        if(rotation < 0 || rotation > 3) throw new IllegalArgumentException("Rotation must be 0-3");
        this.rotation = rotation;
    }

    /**
     * Returns the array of connectors in the tile's default orientation (un-rotated).
     *
     * @return an array of 4 {@link Connector}s
     */
    public Connector[] getConnectors() {
        return connectors;
    }

    /**
     * Returns the connector currently facing the given side after accounting for rotation.
     * The side index follows this order: 0 = top, 1 = right, 2 = bottom, 3 = left.
     *
     * @param side the side to query (0–3)
     * @return the connector at that side after rotation
     * @throws IllegalArgumentException if side is not in the range 0–3
     */
    public Connector getConnector(int side) {
        if(side < 0 || side > 3) throw new IllegalArgumentException("Side must be 0-3");
        return connectors[(side - rotation + 4) % 4];
    }

    /**
     * Returns the tile type (used by subclasses to define tile behavior).
     *
     * @return the type of tile as a string
     */
    public abstract String getTileType();
}