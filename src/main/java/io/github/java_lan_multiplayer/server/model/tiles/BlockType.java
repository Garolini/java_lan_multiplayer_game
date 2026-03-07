package io.github.java_lan_multiplayer.server.model.tiles;

/**
 * Represents the type of block with an associated monetary value.
 * <p>
 */
public enum BlockType {
    RED(4), YELLOW(3), GREEN(2), BLUE(1);

    private final int value;

    BlockType(int value) {
        this.value = value;
    }

    /**
     * Returns the numeric value associated with the block type.
     *
     * @return the integer value of the block
     */
    public int getValue() {
        return value;
    }
}