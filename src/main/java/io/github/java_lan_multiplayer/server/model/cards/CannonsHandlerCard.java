package io.github.java_lan_multiplayer.server.model.cards;

import io.github.java_lan_multiplayer.server.model.Player;

import java.awt.*;
import java.util.List;

/**
 * Interface for cards that allow players to activate cannons.
 */
public interface CannonsHandlerCard {

    /**
     * Activates a specified number of vertical and rotated cannons using provided batteries.
     *
     * @param caller         the player activating cannons
     * @param verticalCannons number of vertical cannons to activate
     * @param rotatedCannons  number of rotated cannons to activate
     * @param batteryTiles    list of coordinates of battery tiles used
     */
    void activateCannons(Player caller, int verticalCannons, int rotatedCannons, List<Point> batteryTiles);
}