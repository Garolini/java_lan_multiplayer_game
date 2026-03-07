package io.github.java_lan_multiplayer.common.messages.flight.cardActions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.BatteryHandlerCard;
import io.github.java_lan_multiplayer.server.model.cards.Card;

import java.awt.*;

/**
 * A message representing a player's decision to use a specific battery tile
 * to activate or enhance the effect of a {@link BatteryHandlerCard}.
 * <p>
 * This message is typically sent during the resolution of a card that offers
 * the player the choice to consume a battery for an additional effect or action.
 * </p>
 *
 * <p>
 * The {@code batteryCoords} field indicates the coordinates of the battery tile
 * on the player's ship grid that is being used.
 * </p>
 *
 * <p>
 * Implements {@link CardActionMessage} and integrates with the game's message
 * dispatch and card-effect system.
 * </p>
 *
 * @see BatteryHandlerCard
 * @see CardActionMessage
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatteryUseMessage implements CardActionMessage {

    private Point batteryCoords;

    public BatteryUseMessage() {}

    public BatteryUseMessage(Point batteryCoords) {
        this.batteryCoords = batteryCoords;
    }

    public Point getBatteryCoords() {
        return batteryCoords;
    }

    @Override
    public String getType() {
        return "use_battery";
    }


    @Override
    public void applyTo(Card card, Player player) {
        if (card instanceof BatteryHandlerCard c) {
            c.useBattery(player, batteryCoords);
        } else {
            Logger.logError(getClass().getSimpleName() + " sent to invalid card: " + card.getClass().getSimpleName());
        }
    }
}
