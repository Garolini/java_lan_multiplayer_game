package io.github.java_lan_multiplayer.server.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.server.events.flight.cards.DecisionEndEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.EngineSelectionEvent;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.tiles.EngineTile;

import java.awt.*;
import java.util.List;

import static io.github.java_lan_multiplayer.server.model.Player.PlayerState.*;
import static io.github.java_lan_multiplayer.server.model.cards.Card.InputState.*;

/**
 * Represents an "Open Space" card where players move forward based on their engine strength.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class OpenSpaceCard extends Card implements EnginesHandlerCard {

    private List<Player> playerQueue;

    public OpenSpaceCard(@JsonProperty("id") int id, @JsonProperty("level") int level) {
        super(id, level);
    }

    /**
     * Activates the card, starting the engine activation process for all players in turn.
     * Initiates the engine selection for the first player in the queue.
     */
    @Override
    protected void onActivate() {
        this.playerQueue = board.getSortedPlayers();

        if (!playerQueue.isEmpty()) {
            setInputState(ENGINE_SELECTION);

            dispatcher.fireEvent(new EngineSelectionEvent(playerQueue.getFirst(), true));
        } else {
            setInputState(FINISHED);
        }
    }

    /**
     * Called when a player activates engines to move forward. Battery usage is validated,
     * and player movement or elimination is processed accordingly.
     *
     * @param caller           the player activating engines
     * @param activatedEngines the number of engines being activated
     * @param batteryTiles     the battery tile coordinates used to power the engines
     * @throws IllegalArgumentException if the number of engines doesn't match the number of batteries used
     */
    @Override
    public void activateEngines(Player caller, int activatedEngines, List<Point> batteryTiles) {
        verifyInputState(ENGINE_SELECTION);
        verifyPlayerTurn(caller, playerQueue);
        dispatcher.fireEvent(new DecisionEndEvent(caller.getName()));
        if(activatedEngines != batteryTiles.size()) throw new IllegalArgumentException("The number of activated engines does not match the number of batteries used.");
        int strength = (int)caller.getStrength(EngineTile.class, activatedEngines);

        if (strength != 0) {
            caller.removeBatteries(batteryTiles);
            board.movePlayerForward(caller, strength);
        } else {
            caller.setState(ELIMINATED);
        }
        playerQueue.removeFirst();

        if (!playerQueue.isEmpty()) {
            dispatcher.fireEvent(new EngineSelectionEvent(playerQueue.getFirst(), true));
        } else {
            setInputState(FINISHED);
        }
    }

    @Override
    public String getCardType() {
        return "Open Space";
    }
}
