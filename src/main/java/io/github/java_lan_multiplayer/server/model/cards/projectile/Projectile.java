package io.github.java_lan_multiplayer.server.model.cards.projectile;

import io.github.java_lan_multiplayer.server.model.Player;

/**
 * Represents a projectile fired at a player's ship, used in combat-related card effects.
 * Projectiles have a size, a direction of origin (source), and a computed or predefined path index.
 * Subclasses must define how the projectile interacts with a player (e.g., whether it can be defended against).
 */
public abstract class Projectile {

    private final Size size;
    private final Source source;
    private Integer pathIndex;

    /**
     * Enum representing the direction from which the projectile originates.
     * Each direction is mapped to an internal integer value used in path calculation.
     */
    public enum Source {
        UP(0), DOWN(2), LEFT(3), RIGHT(1);
        private final int source;
        Source(int source) {
            this.source = source;
        }
        /**
         * Returns the integer representation of the source.
         *
         * @return integer value representing the source direction
         */
        public int toInt() {
            return source;
        }
    }
    /**
     * Enum representing the size of the projectile.
     */
    public enum Size {
        SMALL, LARGE
    }

    /**
     * Constructs a new projectile with the given size and source.
     *
     * @param size   the size of the projectile
     * @param source the direction from which the projectile is fired
     */
    public Projectile(Size size, Source source) {
        this.size = size;
        this.source = source;
    }
    /**
     * Constructs a new projectile with a predefined path index.
     * Used primarily for testing or deterministic scenarios.
     *
     * @param size      the size of the projectile
     * @param source    the direction from which the projectile is fired
     * @param pathIndex the predefined path index (used instead of random dice roll)
     */
    public Projectile(Size size, Source source, int pathIndex) {
        this(size, source);
        this.pathIndex = pathIndex;
    }

    public Size getSize() {
        return size;
    }
    public Source getSource() {
        return source;
    }

    public int getPathIndex() {
        return pathIndex;
    }

    /**
     * Evaluates whether the given player is able to defend against this projectile.
     * Implemented by concrete subclasses to handle different projectile effects.
     *
     * @param player the player to evaluate
     * @return true if the projectile can be defended against; false otherwise
     */
    public abstract boolean evaluateDefense(Player player);

    /**
     * Rolls a single six-sided die.
     *
     * @return a number between 1 and 6
     */
    private static int rollDie() {
        return 1 + (int)(Math.random() * 6);
    }

    /**
     * Randomly determines the path index using two simulated dice rolls.
     * If a predefined path index is already set (e.g., for testing), no change occurs.
     *
     * @return an array of two integers representing the dice rolls,
     *         or {@code null} if the path index was already set
     */
    public int[] randomizePathIndex() {
        // If the value was passed in the constructor (for test purposes), it doesn't override it.
        if(pathIndex != null) return null;

        int firstDie = rollDie();
        int secondDie = rollDie();
        int diceSum = firstDie + secondDie;

        this.pathIndex = switch (source) {
            case UP, DOWN -> diceSum - 4;
            case LEFT, RIGHT -> diceSum - 5;
        };

        // Return the die values in an array
        return new int[] { firstDie, secondDie };
    }
}
