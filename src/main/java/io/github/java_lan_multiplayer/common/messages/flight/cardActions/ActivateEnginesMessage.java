package io.github.java_lan_multiplayer.common.messages.flight.cardActions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.Card;
import io.github.java_lan_multiplayer.server.model.cards.EnginesHandlerCard;

import java.awt.*;
import java.util.List;

/**
 * Message class representing the action of activating engines on a card.
 * <p>
 * This message is sent from the client to the server to signal that
 * a player has chosen to activate a specific number of engines, optionally consuming
 * battery tiles in the process.
 * </p>
 *
 * <p>
 * It implements {@link CardActionMessage} and is handled specifically by
 * {@link EnginesHandlerCard}.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivateEnginesMessage implements CardActionMessage {

    private int activatedEngines;
    private List<Point> batteryTiles;

    public ActivateEnginesMessage() {}

    public ActivateEnginesMessage(int activatedEngines, List<Point> batteryTiles) {
        this.activatedEngines = activatedEngines;
        this.batteryTiles = batteryTiles;
    }

    public int getActivatedEngines() {
        return activatedEngines;
    }
    public List<Point> getBatteryTiles() {
        return batteryTiles;
    }

    @Override
    public String getType() {
        return "activate_engines";
    }


    @Override
    public void applyTo(Card card, Player player) {
        if (card instanceof EnginesHandlerCard c) {
            c.activateEngines(player, activatedEngines, batteryTiles);
        } else {
            Logger.logError(getClass().getSimpleName() + " sent to invalid card: " + card.getClass().getSimpleName());
        }
    }
}
