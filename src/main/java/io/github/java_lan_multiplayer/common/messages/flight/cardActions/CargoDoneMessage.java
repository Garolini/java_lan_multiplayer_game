package io.github.java_lan_multiplayer.common.messages.flight.cardActions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.Card;
import io.github.java_lan_multiplayer.server.model.cards.CargoHandlerCard;

/**
 * A message indicating that a player has completed their cargo selection or manipulation phase
 * for a card handled by a {@link CargoHandlerCard}.
 * <p>
 * This message is typically sent from the client to confirm that the player is done interacting
 * with a cargo-related UI and is ready to proceed with the game logic.
 * </p>
 *
 * <p>
 * Implements {@link CardActionMessage} and is dispatched to the associated card's logic
 * through the {@code applyTo} method.
 * </p>
 *
 * @see CargoHandlerCard
 * @see CardActionMessage
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CargoDoneMessage implements CardActionMessage {

    public CargoDoneMessage() {}

    @Override
    public String getType() {
        return "cargo_done";
    }


    @Override
    public void applyTo(Card card, Player player) {
        if (card instanceof CargoHandlerCard c) {
            c.confirmDone(player);
        } else {
            Logger.logError(getClass().getSimpleName() + " sent to invalid card: " + card.getClass().getSimpleName());
        }
    }
}
