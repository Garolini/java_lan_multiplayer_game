package io.github.java_lan_multiplayer.server.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.server.events.flight.cards.CardDecisionEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.CargoDecisionEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.PlayerStatusEvent;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.tiles.BlockType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static io.github.java_lan_multiplayer.server.model.cards.Card.InputState.*;

/**
 * Represents an Abandoned Station card where players can collect goods if they
 * have enough crew members. Players who accept the card move backward and are
 * rewarded with cargo which they must place using a cargo placement phase.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class AbandonedStationCard extends Card implements DecisionHandlerCard, CargoHandlerCard {

    private final int requiredCrew;
    private final BlockType[] rewardCargo;
    private final int positionCost;

    private List<Player> playerQueue;

    private Player rewardedPlayer;

    public AbandonedStationCard(
            @JsonProperty("id") int id,
            @JsonProperty("level") int level,
            @JsonProperty("requiredCrew") int requiredCrew,
            @JsonProperty("rewardCargo") BlockType[] rewardCargo,
            @JsonProperty("positionCost") int positionCost) {
        super(id, level);
        if (requiredCrew < 0) throw new IllegalArgumentException("positionCost must be a positive number.");
        this.requiredCrew = requiredCrew;
        this.rewardCargo = rewardCargo;
        if (positionCost < 0) throw new IllegalArgumentException("positionCost must be a positive number.");
        this.positionCost = positionCost;
    }

    /**
     * Activates the card, filtering for eligible players and prompting the first one.
     */
    @Override
    protected void onActivate() {
        this.playerQueue = board.getSortedPlayers();
        setInputState(REWARD_DECISION);
        skipIneligiblePlayers();

        if(!playerQueue.isEmpty()) dispatcher.fireEvent(new CardDecisionEvent(playerQueue.getFirst().getName()));
    }

    /**
     * Called when a player accepts the reward. Moves them backward and begins cargo placement.
     *
     * @param caller the player accepting the reward.
     */
    @Override
    public void accept(Player caller) {
        verifyInputState(REWARD_DECISION);
        verifyPlayerTurn(caller, playerQueue);
        board.movePlayerBackward(caller, positionCost);
        this.rewardedPlayer = caller;
        caller.setCargoPool(new ArrayList<>(Arrays.asList(rewardCargo)));
        playerQueue.clear();
        setInputState(CARGO_DECISION);

        dispatcher.fireEvent(new CargoDecisionEvent(Set.of(caller)));
    }
    /**
     * Called when a player refuses the reward. Skips to the next eligible player.
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
     * Skips players who do not meet the crew requirement.
     */
    private void skipIneligiblePlayers() {
        verifyInputState(REWARD_DECISION);
        while(!playerQueue.isEmpty() && playerQueue.getFirst().getCrewCount() < requiredCrew) {
            dispatcher.fireEvent(new PlayerStatusEvent(playerQueue.getFirst().getName(), PlayerStatusEvent.Status.SKIPPED_CREW));
            playerQueue.removeFirst();
        }
        if(playerQueue.isEmpty()) {
            setInputState(FINISHED);
        }
    }

    /**
     * Loads a cargo block into the player's ship.
     *
     * @param caller         the player performing the action.
     * @param blockIndex     the index of the cargo block from the pool.
     * @param tileCoords     the target tile coordinates for placement.
     * @param containerIndex the index of the container being used.
     */
    @Override
    public void loadCargo(Player caller, int blockIndex, Point tileCoords, int containerIndex) {
        verifyInputState(CARGO_DECISION);
        verifyCaller(caller, rewardedPlayer);

        caller.swapCargo(blockIndex, tileCoords, containerIndex);
    }

    /**
     * Unloads a cargo block from the player's ship.
     *
     * @param caller         the player performing the action.
     * @param tileCoords     the tile coordinates from which to unload.
     * @param containerIndex the container index.
     */
    @Override
    public void unloadCargo(Player caller, Point tileCoords, int containerIndex) {
        verifyInputState(CARGO_DECISION);
        verifyCaller(caller, rewardedPlayer);

        caller.unloadCargo(tileCoords, containerIndex);
    }
    /**
     * Confirms that the player has finished placing all their cargo.
     *
     * @param caller the player confirming completion.
     */
    @Override
    public void confirmDone(Player caller) {
        verifyInputState(CARGO_DECISION);
        verifyCaller(caller, rewardedPlayer);
        caller.clearCargoPool();
        setInputState(FINISHED);
    }

    @Override
    public String getCardType() {
        return "Abandoned Station";
    }
}
