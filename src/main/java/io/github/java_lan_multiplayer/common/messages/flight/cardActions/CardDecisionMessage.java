package io.github.java_lan_multiplayer.common.messages.flight.cardActions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.Card;
import io.github.java_lan_multiplayer.server.model.cards.DecisionHandlerCard;

/**
 * A message representing a player's decision to either accept or refuse an action
 * offered by a {@link DecisionHandlerCard}.
 * <p>
 * This message is used to communicate the outcome of a binary decision prompt
 * (e.g., "Do you want to take this action?") that arises during card resolution.
 * </p>
 *
 * <p>
 * The {@code accepted} flag indicates the player's response: {@code true} for acceptance,
 * {@code false} for refusal. The {@code playerName} field identifies the responding player.
 * </p>
 *
 * <p>
 * Implements {@link CardActionMessage} and is dispatched during the resolution of
 * card actions requiring player input.
 * </p>
 *
 * @see DecisionHandlerCard
 * @see CardActionMessage
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDecisionMessage implements CardActionMessage {

    private String playerName;
    private Boolean accepted;

    public CardDecisionMessage() {}

    public CardDecisionMessage(String playerName, Boolean accepted) {
        this.playerName = playerName;
        this.accepted = accepted;
    }

    public String getPlayerName() {
        return playerName;
    }
    public Boolean getAccepted() {
        return accepted;
    }

    @Override
    public String getType() {
        return "card_decision";
    }


    @Override
    public void applyTo(Card card, Player player) {
        if (card instanceof DecisionHandlerCard c) {
            if (accepted) {
                c.accept(player);
            } else {
                c.refuse(player);
            }
        } else {
            Logger.logError(getClass().getSimpleName() + " sent to invalid card: " + card.getClass().getSimpleName());
        }
    }
}
