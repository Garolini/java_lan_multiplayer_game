package io.github.java_lan_multiplayer.server.model.tiles;

import io.github.java_lan_multiplayer.server.model.ShipBoard;

import static io.github.java_lan_multiplayer.server.model.tiles.CrewType.*;

/**
 * Utility class for calculating the strength contribution of activatable tiles (e.g., Cannons, Engines)
 * on a ship board, considering their orientation, type (single/double), and alien support.
 */
public class StrengthCalculator {

    /**
     * Calculates the total strength of the specified activatable tiles on the board.
     *
     * @param board          the ship board to calculate strength from
     * @param tileClass      the class of the activatable tile (e.g., CannonTile or EngineTile)
     * @param doubleVertical number of double tiles activated in vertical (rotation 0) orientation
     * @param doubleRotated  number of double tiles activated in rotated (non-zero rotation) orientation
     * @return the calculated total strength
     * @throws IllegalArgumentException if invalid tile counts or activation constraints are violated
     */
    public static double calculateStrength(ShipBoard board, Class<? extends ActivatableTile> tileClass, int doubleVertical, int doubleRotated) {

        validateActivation(board, tileClass, doubleVertical, doubleRotated);

        double strength = 0;

        int singleVertical = board.getTileCount(tileClass, tile -> !((ActivatableTile)tile).isDouble() && tile.getRotation() == 0);
        int singleRotated = board.getTileCount(tileClass, tile -> !((ActivatableTile)tile).isDouble() && tile.getRotation() != 0);

        strength += singleVertical * getStrength(tileClass, false, 0);
        strength += singleRotated * getStrength(tileClass, false, 1);
        strength += doubleVertical * getStrength(tileClass, true, 0);
        strength += doubleRotated * getStrength(tileClass, true, 1);

        if((tileClass == EngineTile.class && board.hasAlien(BROWN_ALIEN) || tileClass == CannonTile.class && board.hasAlien(PURPLE_ALIEN)) && strength > 0) strength += 2;
        return strength;
    }

    /**
     * Validates whether the given number of double tile activations are allowed.
     *
     * @param board    the board to validate against
     * @param tileClass the tile class to validate
     * @param vertical the number of vertical double tiles to activate
     * @param rotated  the number of rotated double tiles to activate
     * @throws IllegalArgumentException if constraints are violated (e.g., not enough tiles or batteries)
     */
    private static void validateActivation(ShipBoard board, Class<? extends ActivatableTile> tileClass, int vertical, int rotated) {
        int verticalAvailable = board.getTileCount(tileClass, tile -> ((ActivatableTile) tile).isDouble() && tile.getRotation() == 0);
        int rotatedAvailable = board.getTileCount(tileClass, tile -> ((ActivatableTile) tile).isDouble() && tile.getRotation() != 0);

        if(vertical < 0 || rotated < 0) throw new IllegalArgumentException("Cannot activate a negative number of tiles.");
        if(vertical > verticalAvailable || rotated > rotatedAvailable) throw new IllegalArgumentException("Not enough available tiles.");
        if(vertical + rotated > board.getTotalBatteriesCount()) throw new IllegalArgumentException("Not enough batteries to activate all the tiles.");
    }

    /**
     * Gets the strength value of a tile based on its class, type, and orientation.
     *
     * @param tileClass the tile class (must be CannonTile or EngineTile)
     * @param isDouble  whether the tile is double-sized
     * @param rotation  the tile rotation (0 for vertical)
     * @return the strength value
     * @throws IllegalArgumentException if the tile type is unsupported
     */
    private static double getStrength(Class<? extends ActivatableTile> tileClass, boolean isDouble, int rotation) {
        if(tileClass == CannonTile.class) {
            return CannonTile.getStrength(rotation, isDouble);
        } else if(tileClass == EngineTile.class) {
            return (rotation == 0)? EngineTile.getStrength(0, isDouble) : 0;
        }
        throw new IllegalArgumentException("Unsupported tile type: " + tileClass);
    }
}