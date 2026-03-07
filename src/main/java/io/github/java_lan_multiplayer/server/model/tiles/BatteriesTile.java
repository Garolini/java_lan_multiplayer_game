package io.github.java_lan_multiplayer.server.model.tiles;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a tile that contains a battery storage component.
 * <p>
 * A {@code BatteriesTile} has a limited capacity (2 or 3) for storing power cells (batteries),
 * which can be consumed or recharged during gameplay. Batteries are commonly used to power
 * other tiles such as shields or weapons.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class BatteriesTile extends Tile {

    private final int capacity;
    private int storedBatteries;

    /**
     * Constructs a {@code BatteriesTile} with the specified ID, connectors, and battery capacity.
     *
     * @param id         the unique identifier of the tile
     * @param connectors the array of {@link Connector} objects representing the tile’s sides
     * @param capacity   the battery capacity (must be 2 or 3)
     * @throws IllegalArgumentException if the capacity is not 2 or 3
     */
    public BatteriesTile(@JsonProperty("id") int id, @JsonProperty("connectors") Connector[] connectors, @JsonProperty("capacity") int capacity) {
        super(id,connectors);
        if (capacity < 2 || capacity > 3) throw new IllegalArgumentException("Battery Component must have capacity of 2 or 3.");
        this.capacity = capacity;
        this.storedBatteries = capacity;
    }

    /**
     * Returns the total battery capacity of the tile.
     *
     * @return the capacity (2 or 3)
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the current number of stored batteries.
     *
     * @return the current battery count
     */
    public int getStoredBatteries() {
        return storedBatteries;
    }

    /**
     * Attempts to remove one battery from the storage.
     *
     * @return {@code true} if a battery was removed; {@code false} if no batteries were available
     */
    public boolean removeBattery() {
        if(storedBatteries <= 0) return false;
        storedBatteries--;
        return true;
    }

    public boolean isFull() {
        return storedBatteries == capacity;
    }

    @Override
    public String getTileType() {
        return "Battery Container";
    }
}
