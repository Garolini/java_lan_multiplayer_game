package io.github.java_lan_multiplayer.common.messages.flight.cardActions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.Card;
import io.github.java_lan_multiplayer.server.model.cards.CargoHandlerCard;

import java.awt.*;

/**
 * A message representing a cargo swap operation initiated by a player during a card effect
 * handled by a {@link CargoHandlerCard}.
 * <p>
 * This message contains detailed information about the cargo block being moved,
 * including its index, target coordinates on the ship, and the container it came from.
 * </p>
 *
 * <p>
 * When applied, the {@code loadCargo} method of the appropriate card is invoked to perform the swap.
 * </p>
 *
 * @see CargoHandlerCard
 * @see CardActionMessage
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CargoSwapMessage implements CardActionMessage {

    private String playerName;
    private int blockIndex;
    private Point tileCoords;
    private int containerIndex;

    public CargoSwapMessage() {}

    public CargoSwapMessage(String playerName, int blockIndex, Point tileCoords, int containerIndex) {
        this.playerName = playerName;
        this.blockIndex = blockIndex;
        this.tileCoords = tileCoords;
        this.containerIndex = containerIndex;
    }

    public String getPlayerName() {
        return playerName;
    }
    public int getBlockIndex() {
        return blockIndex;
    }
    public Point getTileCoords() {
        return tileCoords;
    }
    public int getContainerIndex() {
        return containerIndex;
    }

    @Override
    public String getType() {
        return "swap_cargo";
    }


    @Override
    public void applyTo(Card card, Player player) {
        if (card instanceof CargoHandlerCard c) {
            c.loadCargo(player, blockIndex, tileCoords, containerIndex);
        } else {
            Logger.logError(getClass().getSimpleName() + " sent to invalid card: " + card.getClass().getSimpleName());
        }
    }
}
