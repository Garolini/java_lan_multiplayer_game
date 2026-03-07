package io.github.java_lan_multiplayer.server.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.server.events.flight.cards.CardDecisionEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.CrewDecisionEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.DecisionEndEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.PlayerStatusEvent;
import io.github.java_lan_multiplayer.server.model.Player;

import java.awt.*;
import java.util.List;

import static io.github.java_lan_multiplayer.server.model.cards.Card.InputState.*;

/**
 * Represents a card where players can trade crew members for credits,
 * simulating the discovery of an abandoned ship.
 * <p>
 * This card gives players the option to accept a reward (credits) by
 * giving up a certain number of crew members and moving backward on the board.
 * Players with insufficient crew are skipped automatically.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class AbandonedShipCard extends Card implements DecisionHandlerCard, CrewHandlerCard {

    private final int crewToGiveUp;
    private final int rewardCredits;
    private final int positionCost;

    private List<Player> playerQueue;
    private Player rewardedPlayer;
    private int remainingCrewToGiveUp;

    public AbandonedShipCard(
            @JsonProperty("id") int id,
            @JsonProperty("level") int level,
            @JsonProperty("crewToGiveUp") int crewToGiveUp,
            @JsonProperty("rewardCredits") int rewardCredits,
            @JsonProperty("positionCost") int positionCost) {
        super(id, level);
        if (crewToGiveUp < 0) throw new IllegalArgumentException("crewToGiveUp must be a positive number.");
        this.crewToGiveUp = crewToGiveUp;
        if (rewardCredits < 0) throw new IllegalArgumentException("rewardCredits must be a positive number.");
        this.rewardCredits = rewardCredits;
        if (positionCost < 0) throw new IllegalArgumentException("travelCost must be a positive number.");
        this.positionCost = positionCost;
    }

    /**
     * Initializes the card and starts the reward decision phase by prompting the first eligible player.
     */
    @Override
    protected void onActivate() {
        this.playerQueue = board.getSortedPlayers();
        setInputState(REWARD_DECISION);
        skipIneligiblePlayers();

        if(!playerQueue.isEmpty()) dispatcher.fireEvent(new CardDecisionEvent(playerQueue.getFirst().getName()));
    }

    /**
     * Called when a player accepts the reward. Transfers credits, moves the player,
     * and transitions to crew removal phase if necessary.
     *
     * @param caller the player accepting the reward.
     */
    @Override
    public void accept(Player caller) {
        verifyInputState(REWARD_DECISION);
        verifyPlayerTurn(caller, playerQueue);
        if(caller.getCrewCount() <= crewToGiveUp) throw new IllegalStateException("Caller should have been skipped.");
        caller.giveCredits(rewardCredits);
        board.movePlayerBackward(caller, positionCost);
        playerQueue.clear();
        if(crewToGiveUp > 0) {
            this.rewardedPlayer = caller;
            this.remainingCrewToGiveUp = crewToGiveUp;
            setInputState(CREW_DECISION);

            dispatcher.fireEvent(new CrewDecisionEvent(caller.getName(), caller.getShipContent().crewData()));
        }
        else setInputState(FINISHED);
    }

    /**
     * Removes a crew member from the rewarded player's ship. Called repeatedly
     * until the required number of crew are removed.
     *
     * @param caller      the player performing the removal (must be the rewarded player).
     * @param cabinCoords the coordinates of the cabin from which to remove a crew member.
     */
    @Override
    public void removeMemberFrom(Player caller, Point cabinCoords) {
        verifyInputState(CREW_DECISION);
        verifyCaller(caller, rewardedPlayer);
        if(remainingCrewToGiveUp <= 0) throw new RuntimeException("No more crew members are required to be removed.");
        caller.ensureCrewRemovedAt(cabinCoords);
        remainingCrewToGiveUp--;
        if(remainingCrewToGiveUp == 0) {
            dispatcher.fireEvent(new DecisionEndEvent(caller.getName()));
            setInputState(FINISHED);
        }
    }

    /**
     * Called when a player refuses the reward. Removes the player from the queue
     * and prompts the next eligible player.
     *
     * @param caller the player refusing the reward.
     */
    @Override
    public void refuse(Player caller) {
        verifyInputState(REWARD_DECISION);
        verifyPlayerTurn(caller, playerQueue);
        playerQueue.removeFirst();
        skipIneligiblePlayers();

        if(!playerQueue.isEmpty()) dispatcher.fireEvent(new CardDecisionEvent(playerQueue.getFirst().getName()));
    }

    /**
     * Skips players in the queue who do not have enough crew members to claim the reward.
     */
    private void skipIneligiblePlayers() {
        verifyInputState(REWARD_DECISION);
        while(!playerQueue.isEmpty() && playerQueue.getFirst().getCrewCount() <= crewToGiveUp) {
            dispatcher.fireEvent(new PlayerStatusEvent(playerQueue.getFirst().getName(), PlayerStatusEvent.Status.SKIPPED_CREW));
            playerQueue.removeFirst();
        }
        if(playerQueue.isEmpty()) {
            setInputState(FINISHED);
        }
    }

    @Override
    public String getCardType() {
        return "Abandoned Ship";
    }
}
