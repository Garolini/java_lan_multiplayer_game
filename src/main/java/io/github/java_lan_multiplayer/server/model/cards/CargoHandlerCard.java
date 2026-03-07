package io.github.java_lan_multiplayer.server.model.cards;

import io.github.java_lan_multiplayer.server.model.Player;

import java.awt.*;

/**
 * Interface for cards that allow a player to manage cargo during a card event.
 */
public interface CargoHandlerCard {

    /**
     * Loads a cargo block from the temporary pool to a specified container on the player's ship.
     *
     * @param player         the player performing the action
     * @param blockIndex     index of the cargo block in the temporary cargo pool
     * @param tileCoords     coordinates of the tile where the cargo is being placed
     * @param containerIndex index of the cargo container on the target tile
     */
    void loadCargo(Player player, int blockIndex, Point tileCoords, int containerIndex);

    /**
     * Unloads a cargo block from a container on the player's ship back to the temporary pool.
     *
     * @param caller         the player performing the action
     * @param tileCoords     coordinates of the tile from which the cargo is being removed
     * @param containerIndex index of the cargo container being emptied
     */
    void unloadCargo(Player caller, Point tileCoords, int containerIndex);

    /**
     * Confirms that the player has finished loading/unloading cargo.
     *
     * @param caller the player who is done with cargo management
     */
    void confirmDone(Player caller);
}