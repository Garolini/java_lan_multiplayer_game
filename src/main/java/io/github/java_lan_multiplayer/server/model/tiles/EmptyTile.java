package io.github.java_lan_multiplayer.server.model.tiles;

/**
 * A placeholder tile representing an empty space.
 * Typically used for representing unoccupied grid positions.
 */
public class EmptyTile extends Tile {

    /**
     * Constructs an EmptyTile with ID 0 and no connectors.
     */
    public EmptyTile() {
        super(0, new Connector[4]);
    }

    @Override
    public String getTileType() {
        return "Empty";
    }
}
