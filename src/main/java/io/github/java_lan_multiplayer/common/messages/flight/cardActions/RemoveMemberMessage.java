package io.github.java_lan_multiplayer.common.messages.flight.cardActions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.Card;
import io.github.java_lan_multiplayer.server.model.cards.CrewHandlerCard;

import java.awt.*;

/**
 * A message indicating that a crew member should be removed from a specified cabin
 * during a {@link CrewHandlerCard} event.
 * <p>
 * This message is typically sent by a client to inform the server that a crew member
 * was removed from the provided cabin coordinates on the player's ship.
 * It triggers the {@code removeMemberFrom} method on the {@code CrewHandlerCard}.
 * </p>
 *
 * @see CrewHandlerCard
 * @see CardActionMessage
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoveMemberMessage implements CardActionMessage {

    private Point cabinCoords;

    public RemoveMemberMessage() {}

    public RemoveMemberMessage(Point cabinCoords) {
        this.cabinCoords = cabinCoords;
    }
    public Point getCabinCoords() {
        return cabinCoords;
    }

    @Override
    public String getType() {
        return "remove_member";
    }


    @Override
    public void applyTo(Card card, Player player) {
        if (card instanceof CrewHandlerCard c) {
            c.removeMemberFrom(player, cabinCoords);
        } else {
            Logger.logError(getClass().getSimpleName() + " sent to invalid card: " + card.getClass().getSimpleName());
        }
    }
}
