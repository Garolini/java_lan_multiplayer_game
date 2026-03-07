package io.github.java_lan_multiplayer.server.model.tiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a tile that provides support from an alien crew member.
 * <p>
 * This tile is associated with a specific alien {@link CrewType} and may have unique in-game effects
 * related to alien interactions or support bonuses.
 * </p>
 * <p>
 * This tile is only valid with crew types classified as aliens.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class AlienSupportTile extends Tile {

    private final CrewType alienType;

    /**
     * Constructs an {@code AlienSupportTile} with the given ID, connectors, and alien crew type.
     *
     * @param id         the unique identifier of the tile
     * @param connectors an array of 4 {@link Connector}s representing the tile’s sides
     * @param alienType  the {@link CrewType} representing the alien; must be a valid alien type
     * @throws IllegalArgumentException if the provided crew type is not an alien
     */
    public AlienSupportTile(@JsonProperty("id") int id, @JsonProperty("connectors") Connector[] connectors, @JsonProperty("alienType") CrewType alienType) {
        super(id, connectors);
        if(!alienType.isAlien()) throw new IllegalArgumentException(alienType + " is a not a valid alien type.");
        this.alienType = alienType;
    }


    /**
     * Returns the alien crew type associated with this tile.
     *
     * @return the alien {@link CrewType}
     */
    public CrewType getAlienColor() {
        return alienType;
    }

    @Override
    public String getTileType() {
        return "Alien Support";
    }
}