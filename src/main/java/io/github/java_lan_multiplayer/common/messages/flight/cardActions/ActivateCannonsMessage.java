package io.github.java_lan_multiplayer.common.messages.flight.cardActions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.CannonsHandlerCard;
import io.github.java_lan_multiplayer.server.model.cards.Card;

import java.awt.*;
import java.util.List;

/**
 * A message representing a player's action to activate cannons during a card effect resolution.
 * <p>
 * This message contains data about the number of vertical and rotated cannons being activated,
 * as well as the battery tiles used to power them. It is intended to be sent from the client
 * to the server during cannon-related card effects.
 * </p>
 *
 * <p>
 * Upon receipt, the message's {@link #applyTo(Card, Player)} method delegates execution to a
 * {@link CannonsHandlerCard}, which processes the actual game logic for cannon activation.
 * </p>
 *
 * <p>
 * This class is part of the game's card-action messaging system and implements
 * {@link CardActionMessage}, allowing it to be serialized and dispatched like other
 * gameplay messages.
 * </p>
 *
 * @see CannonsHandlerCard
 * @see CardActionMessage
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivateCannonsMessage implements CardActionMessage {

    private int verticalCannons;
    private int rotatedCannons;
    private List<Point> batteryTiles;

    public ActivateCannonsMessage() {}

    public ActivateCannonsMessage(int verticalCannons, int rotatedCannons, List<Point> batteryTiles) {
        this.verticalCannons = verticalCannons;
        this.rotatedCannons = rotatedCannons;
        this.batteryTiles = batteryTiles;
    }

    public int getVerticalCannons() {
        return verticalCannons;
    }
    public int getRotatedCannons() {
        return rotatedCannons;
    }
    public List<Point> getBatteryTiles() {
        return batteryTiles;
    }

    @Override
    public String getType() {
        return "activate_cannons";
    }


    @Override
    public void applyTo(Card card, Player player) {
        if (card instanceof CannonsHandlerCard c) {
            c.activateCannons(player, verticalCannons, rotatedCannons, batteryTiles);
        } else {
            Logger.logError(getClass().getSimpleName() + " sent to invalid card: " + card.getClass().getSimpleName());
        }
    }
}
