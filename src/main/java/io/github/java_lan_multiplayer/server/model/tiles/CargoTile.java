package io.github.java_lan_multiplayer.server.model.tiles;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a cargo tile capable of storing colored blocks with varying capacity and constraints
 * depending on the cargo type (NORMAL or RED).
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class CargoTile extends Tile {

    /**
     * Enum representing the cargo tile type.
     * NORMAL cargo can hold 2–3 non-RED blocks.
     * RED cargo can hold 1–2 RED blocks.
     */
    public enum CargoType {
        NORMAL, RED
    }
    private final CargoType type;
    private final int capacity;
    private final BlockType[] containers;

    /**
     * Constructs a new CargoTile.
     *
     * @param id        the tile ID
     * @param connectors the tile connectors
     * @param type       the cargo type (NORMAL or RED)
     * @param capacity   the number of cargo slots; must match allowed limits for the given type
     * @throws IllegalArgumentException if capacity is outside allowed bounds
     */
    public CargoTile(
            @JsonProperty("id") int id,
            @JsonProperty("connectors") Connector[] connectors,
            @JsonProperty("cargoType") CargoType type,
            @JsonProperty("capacity") int capacity) {
        super(id, connectors);
        this.type = type;
        this.capacity = capacity;
        if (type == CargoType.NORMAL && (capacity < 2 || capacity > 3)) throw new IllegalArgumentException("Cargo must have 2 or 3 containers.");
        if (type == CargoType.RED && (capacity < 1 || capacity > 2)) throw new IllegalArgumentException("Red cargo must have 1 or 2 containers.");
        this.containers = new BlockType[capacity];
    }

    /**
     * Returns the maximum number of blocks this tile can hold.
     *
     * @return the cargo capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the cargo type (NORMAL or RED).
     *
     * @return the cargo type
     */
    public CargoType getType() {
        return type;
    }

    /**
     * Returns the block stored at the given index.
     *
     * @param index the cargo slot index
     * @return the block at the index or {@code null} if empty
     * @throws IllegalArgumentException if the index is invalid
     */
    public BlockType getBlockAt(int index) {
        if (index < 0 || index >= containers.length) {
            throw new IllegalArgumentException("Invalid container index");
        }
        return containers[index];
    }

    /**
     * Loads a block into the specified cargo slot.
     * If a block already exists, it is replaced and returned.
     *
     * @param index    the index to load into
     * @param newBlock the block to load (can be null to unload)
     * @return the block that was previously in the slot, or {@code null} if empty
     * @throws IllegalArgumentException if index is invalid
     * @throws IllegalStateException    if a red block is added to a NORMAL cargo
     */
    public BlockType loadBlock(int index, BlockType newBlock) {
        // if the slot is empty, it adds the block and returns null
        // if there is already a block, it switches them and returns the block that was inside
        if(type == CargoType.NORMAL && newBlock == BlockType.RED) throw new IllegalStateException("Blue cargo cannot hold RED blocks.");
        if(index < 0 || index >= containers.length) throw new IllegalArgumentException("Invalid container index");

        BlockType previousBlock = containers[index];
        containers[index] = newBlock;
        return previousBlock;
    }

    /**
     * Removes and returns the block at the specified slot.
     *
     * @param index the index to unload
     * @return the removed block, or {@code null} if none
     */
    public BlockType unloadBlock(int index) {
        return loadBlock(index, null);
    }

    /**
     * Calculates the total value of all stored blocks.
     *
     * @return the total cargo value
     */
    public int getHoldsValue() {
        int total = 0;
        for (BlockType container : containers) {
            if(container != null) total += container.getValue();
        }
        return total;
    }

    /**
     * Returns the current contents of the cargo hold.
     *
     * @return an array of blocks, some of which may be null
     */
    public BlockType[] getContent() {
        return containers;
    }

    @Override
    public String getTileType() {
        return "Cargo Hold";
    }
}