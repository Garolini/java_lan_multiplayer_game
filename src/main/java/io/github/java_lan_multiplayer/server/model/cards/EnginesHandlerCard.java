package io.github.java_lan_multiplayer.server.model.cards;

import io.github.java_lan_multiplayer.server.model.Player;

import java.awt.*;
import java.util.List;

/**
 * Interface for cards that allow players to activate engines to move their ship, usually consuming battery power.
 */
public interface EnginesHandlerCard {

    /**
     * Activates a number of engines on the player's ship using battery tiles.
     *
     * @param caller           the player activating the engines
     * @param activatedEngines the number of engines the player intends to activate
     * @param batteryTiles     list of battery tile coordinates used to power the engines
     */
    void activateEngines(Player caller, int activatedEngines, List<Point> batteryTiles);
}