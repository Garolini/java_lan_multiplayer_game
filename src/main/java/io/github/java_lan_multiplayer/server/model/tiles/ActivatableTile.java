package io.github.java_lan_multiplayer.server.model.tiles;

/**
 * An abstract subclass of {@link Tile} that represents a tile with activation behavior.
 * <p>
 * Activatable tiles may also be classified as "double" tiles, which may represent tiles that
 * span more than one unit or have enhanced effects depending on game rules.
 * </p>
 */
public abstract class ActivatableTile extends Tile {

    private final boolean isDouble;

    /**
     * Constructs an {@code ActivatableTile} with the specified ID, connectors, and double-tile flag.
     *
     * @param id         the unique identifier for the tile
     * @param connectors an array of 4 {@link Connector}s representing the tile’s sides
     * @param isDouble   whether the tile is considered a "double" tile
     */
    public ActivatableTile(int id,Connector[] connectors, boolean isDouble) {
        super(id, connectors);
        this.isDouble = isDouble;
    }

    /**
     * Returns whether this tile is a "double" tile.
     *
     * @return {@code true} if this is a double tile; {@code false} otherwise
     */
    public boolean isDouble() {
        return isDouble;
    }
}