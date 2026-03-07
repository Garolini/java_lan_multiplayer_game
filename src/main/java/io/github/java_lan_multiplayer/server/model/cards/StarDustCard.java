package io.github.java_lan_multiplayer.server.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.server.events.flight.cards.CardStatusEvent;
import io.github.java_lan_multiplayer.server.model.Player;

import java.util.List;

import static io.github.java_lan_multiplayer.server.model.cards.Card.InputState.*;

/**
 * Represents a Star Dust event card that penalizes players based on their ship's design.
 * Each player loses flight days equal to the number of exposed connectors on their ship.
 * Players are processed in reverse order of the current standings.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class StarDustCard extends Card {
    // Players lose flight days based on exposed connectors.

    public StarDustCard(@JsonProperty("id")int id, @JsonProperty("level")int level) {
        super(id, level);
    }

    /**
     * Activates the Star Dust card's effect.
     * Each player is evaluated for the number of exposed connectors on their ship,
     * and is moved backward accordingly on the flight track.
     * The last player is evaluated first.
     */
    @Override
    protected void onActivate() {
        dispatcher.fireEvent(new CardStatusEvent(CardStatusEvent.CardStatus.STARDUST));
        List<Player> playersReversed = board.getSortedPlayers().reversed();
        for(Player player : playersReversed) {
            int exposedConnectors = player.getExposedConnectorsCount();
            board.movePlayerBackward(player, exposedConnectors);
        }
        setInputState(FINISHED);
    }

    @Override
    public String getCardType() {
        return "Star Dust";
    }
}
