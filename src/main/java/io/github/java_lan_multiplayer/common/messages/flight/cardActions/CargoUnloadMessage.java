package io.github.java_lan_multiplayer.common.messages.flight.cardActions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.Card;
import io.github.java_lan_multiplayer.server.model.cards.CargoHandlerCard;

import java.awt.*;

/**
 * A message representing a cargo unload action initiated by a player during a card effect
 * handled by a {@link CargoHandlerCard}.
 * <p>
 * This message specifies the cargo to unload based on the tile coordinates on the ship and
 * the index of the container it should be placed into.
 * </p>
 *
 * <p>
 * When applied, the {@code unloadCargo} method of the appropriate card is invoked,
 * executing the logic to remove the cargo block from the ship and transfer it to a container.
 * </p>
 *
 * @see CargoHandlerCard
 * @see CardActionMessage
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CargoUnloadMessage implements CardActionMessage {

    private String playerName;
    private Point tileCoords;
    private int containerIndex;

    public CargoUnloadMessage() {}

    public CargoUnloadMessage(String playerName, Point tileCoords, int containerIndex) {
        this.playerName = playerName;
        this.tileCoords = tileCoords;
        this.containerIndex = containerIndex;
    }

    public String getPlayerName() {
        return playerName;
    }
    public Point getTileCoords() {
        return tileCoords;
    }
    public int getContainerIndex() {
        return containerIndex;
    }

    @Override
    public String getType() {
        return "unload_cargo";
    }


    @Override
    public void applyTo(Card card, Player player) {
        if (card instanceof CargoHandlerCard c) {
            c.unloadCargo(player, tileCoords, containerIndex);
        } else {
            Logger.logError(getClass().getSimpleName() + " sent to invalid card: " + card.getClass().getSimpleName());
        }
    }
}
