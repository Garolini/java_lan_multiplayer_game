package io.github.java_lan_multiplayer.server.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.server.events.flight.cards.CargoDecisionEvent;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.tiles.BlockType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static io.github.java_lan_multiplayer.server.model.cards.Card.InputState.*;

/**
 * Represents a Smugglers enemy encounter card.
 * In this combat event, players must meet a required cannon power to avoid punishment.
 * If successful, the player is rewarded with cargo blocks. If defeated, the player loses cargo.
 * <p>
 * Implements cargo handling for both rewarding and punishing players.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class SmugglersCard extends AbstractEnemy implements CargoHandlerCard {
    // Combat encounter, win to gain goods, lose to lose goods.

    private final BlockType[] rewardCargo;
    private final int cargoLossOnDefeat;

    private Player rewardedPlayer;

    public SmugglersCard(
            @JsonProperty("id") int id,
            @JsonProperty("level") int level,
            @JsonProperty("requiredCannonPower") int requiredCannonPower,
            @JsonProperty("rewardCargo")BlockType[] rewardCargo,
            @JsonProperty("positionCost")int positionCost,
            @JsonProperty("cargoLossOnDefeat")int cargoLossOnDefeat) {
        super(id, level, requiredCannonPower, positionCost);
        this.rewardCargo = rewardCargo;
        if (cargoLossOnDefeat < 0) throw new IllegalArgumentException("cargoLossOnDefeat must be a positive number.");
        this.cargoLossOnDefeat = cargoLossOnDefeat;
    }

    /**
     * Represents a Smugglers enemy encounter card.
     * In this combat event, players must meet a required cannon power to avoid punishment.
     * If successful, the player is rewarded with cargo blocks. If defeated, the player loses cargo.
     * <p>
     * Implements cargo handling for both rewarding and punishing players.
     */
    @Override
    protected void giveReward(Player player) {
        verifyInputState(NONE);
        this.rewardedPlayer = player;
        player.setCargoPool(new ArrayList<>(Arrays.asList(rewardCargo)));
        setInputState(CARGO_DECISION);

        dispatcher.fireEvent(new CargoDecisionEvent(Set.of(player)));
    }

    /**
     * Loads a cargo block from the cargo pool into the player's ship.
     *
     * @param caller         the player placing the cargo
     * @param blockIndex     the index of the cargo block in the reward pool
     * @param tileCoords     the tile coordinates where the block will be placed
     * @param containerIndex the index within the container at the given coordinates
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
     * @param caller         the player unloading cargo
     * @param tileCoords     the coordinates of the cargo tile
     * @param containerIndex the index within the container from which to unload
     */
    @Override
    public void unloadCargo(Player caller, Point tileCoords, int containerIndex) {
        verifyInputState(CARGO_DECISION);
        verifyCaller(caller, rewardedPlayer);

        caller.unloadCargo(tileCoords, containerIndex);
    }

    /**
     * Confirms that the player has completed their cargo placement.
     * This clears the cargo pool and ends the interaction.
     *
     * @param caller the player confirming the cargo decision
     */
    @Override
    public void confirmDone(Player caller) {
        verifyInputState(CARGO_DECISION);
        verifyCaller(caller, rewardedPlayer);
        caller.clearCargoPool();
        setInputState(FINISHED);
    }

    /**
     * Applies punishment to the player who failed the encounter.
     * This removes a number of their most valuable cargo blocks.
     *
     * @param player the player who failed the combat
     */
    @Override
    protected void applyPunishment(Player player) {
        verifyInputState(NONE);
        player.removeMostValuableCargo(cargoLossOnDefeat);
        handleNextPlayer();
    }

    @Override
    public String getCardType() {
        return "Smugglers";
    }
}
