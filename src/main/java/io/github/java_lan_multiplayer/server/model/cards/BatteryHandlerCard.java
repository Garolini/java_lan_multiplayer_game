package io.github.java_lan_multiplayer.server.model.cards;

import io.github.java_lan_multiplayer.server.model.Player;

import java.awt.*;

/**
 * Interface for handling battery usage actions on cards.
 */
public interface BatteryHandlerCard {

    /**
     * Requests the use of a battery located at the given coordinates.
     *
     * @param caller        the player using the battery
     * @param batteryCoords the coordinates of the battery tile
     */
    void useBattery(Player caller, Point batteryCoords);

    /**
     * Called when the player refuses to use a battery.
     *
     * @param caller the player refusing the battery usage
     */
    void refuseBattery(Player caller);
}