package io.github.java_lan_multiplayer.server.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.server.events.flight.cards.CardStatusEvent;
import io.github.java_lan_multiplayer.server.model.Player;

import java.util.List;

import static io.github.java_lan_multiplayer.server.model.cards.Card.InputState.*;

/**
 * Represents an Epidemic event card in the game.
 * <p>
 * When activated, this card causes all players to suffer from an epidemic,
 * triggering the loss of crew from connected cabins based on individual player logic.
 * The effect is applied immediately to all players, and no player decisions are required.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class EpidemicCard extends Card {

    public EpidemicCard(@JsonProperty("id") int id, @JsonProperty("level") int level) {
        super(id, level);
    }

    /**
     * Activates the epidemic effect for all players.
     * Dispatches a global epidemic event and applies the epidemic effect to each player.
     * Once complete, the card is marked as finished.
     */

    @Override
    protected void onActivate() {
        dispatcher.fireEvent(new CardStatusEvent(CardStatusEvent.CardStatus.EPIDEMIC));
        List<Player> players = board.getSortedPlayers();
        for(Player player : players) {
            player.applyEpidemicCard();
        }
        setInputState(FINISHED);
    }


    @Override
    public String getCardType() {
        return "Epidemic";
    }
}
