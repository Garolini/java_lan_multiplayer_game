package io.github.java_lan_multiplayer.common.messages.flight.cardActions;

import io.github.java_lan_multiplayer.common.messages.GameMessage;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.Card;

/**
 * Represents an action message that can be applied to a {@link Card} in the game.
 * <p>
 * This interface is used for messages that describe how a player interacts with or activates
 * a card, typically during gameplay. Implementations should define how to apply the action
 * to the card and player.
 * </p>
 *
 * <p>
 * This interface extends {@link GameMessage}, allowing it to be serialized and routed
 * like other network messages in the game system.
 * </p>
 */
public interface CardActionMessage extends GameMessage {

    /**
     * Applies the action defined in the message to the given card for the specified player.
     * <p>
     * This method should encapsulate all necessary logic to interpret and execute the action,
     * including any relevant validation or state changes.
     * </p>
     *
     * @param card   the card on which the action should be applied
     * @param player the player performing the action
     */
    void applyTo(Card card, Player player);
}
