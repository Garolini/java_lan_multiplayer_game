package io.github.java_lan_multiplayer.server.model.cards;

import io.github.java_lan_multiplayer.server.model.Player;

import java.awt.*;

/**
 * Interface for cards that require players to remove crew members from their ship.
 */
public interface CrewHandlerCard {

    /**
     * Removes a crew member from a specified cabin tile on the player's ship.
     *
     * @param caller      the player performing the removal
     * @param cabinCoords coordinates of the cabin tile from which the crew member is to be removed
     */
    void removeMemberFrom(Player caller, Point cabinCoords);
}