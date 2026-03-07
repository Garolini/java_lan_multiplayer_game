package io.github.java_lan_multiplayer.server.model.tiles;

/**
 * Represents the type of crew that can occupy a tile.
 * Includes human and alien crew types, as well as NONE.
 */
public enum CrewType {
    NONE,
    SINGLE_HUMAN,
    DOUBLE_HUMAN,
    BROWN_ALIEN,
    PURPLE_ALIEN;

    /**
     * Checks if the crew type represents a human (single or double).
     *
     * @return {@code true} if the crew is human; {@code false} otherwise
     */
    public boolean isHuman() {
        return this == SINGLE_HUMAN || this == DOUBLE_HUMAN;
    }

    /**
     * Checks if the crew type represents an alien.
     *
     * @return {@code true} if the crew is alien; {@code false} otherwise
     */
    public boolean isAlien() {
        return this == BROWN_ALIEN || this == PURPLE_ALIEN;
    }

    /**
     * Returns the number of humans represented by this crew type.
     *
     * @return 2 for DOUBLE_HUMAN, 1 for SINGLE_HUMAN, 0 otherwise
     */
    public int humanCount() {
        return switch (this) {
            case DOUBLE_HUMAN -> 2;
            case SINGLE_HUMAN -> 1;
            default -> 0;
        };
    }
}