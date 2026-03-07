package io.github.java_lan_multiplayer.common.messages.flight.cardActions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.BatteryHandlerCard;
import io.github.java_lan_multiplayer.server.model.cards.Card;

/**
 * A message indicating that a player has chosen to refuse using a battery
 * for a particular card effect that offers the option.
 * <p>
 * This message is used in the card-action phase of the game where the player
 * can opt not to consume a battery to activate or enhance a card's ability.
 * </p>
 *
 * <p>
 * Upon being applied, this message delegates control to a {@link BatteryHandlerCard},
 * which then executes the logic corresponding to the refusal of battery use.
 * </p>
 *
 * <p>
 * This class implements {@link CardActionMessage} and integrates into the game's
 * message-handling infrastructure.
 * </p>
 *
 * @see BatteryHandlerCard
 * @see CardActionMessage
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatteryRefuseMessage implements CardActionMessage {

    public BatteryRefuseMessage() {}

    @Override
    public String getType() {
        return "refuse_battery";
    }


    @Override
    public void applyTo(Card card, Player player) {
        if (card instanceof BatteryHandlerCard c) {
            c.refuseBattery(player);
        } else {
            Logger.logError(getClass().getSimpleName() + " sent to invalid card: " + card.getClass().getSimpleName());
        }
    }
}
