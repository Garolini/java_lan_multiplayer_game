package io.github.java_lan_multiplayer.server.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.server.events.flight.cards.BatteryDecisionEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.DecisionEndEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.DiceThrowEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.ProjectileEvent;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.projectile.CannonShot;

import java.awt.*;
import java.util.*;
import java.util.List;

import static io.github.java_lan_multiplayer.server.model.cards.Card.InputState.*;

/**
 * Represents a "Pirates" enemy card, where players must meet cannon power requirements to avoid damage.
 * Players who win gain credits; those who fail must endure cannon shots and may defend using batteries.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class PiratesCard extends AbstractEnemy implements BatteryHandlerCard {

    private final int rewardCredits;
    private final CannonShot[] cannonShots;

    private List<CannonShot> shotQueue;
    private Player punishedPlayer;
    private CannonShot currentShot;

    public PiratesCard(
            @JsonProperty("id") int id,
            @JsonProperty("level") int level,
            @JsonProperty("requiredCannonPower") int requiredCannonPower,
            @JsonProperty("rewardCredits") int rewardCredits,
            @JsonProperty("positionCost") int positionCost,
            @JsonProperty("cannonShots") CannonShot[] cannonShots) {
        super(id, level, requiredCannonPower, positionCost);
        if (rewardCredits < 0) throw new IllegalArgumentException("rewardCredits must be a positive number.");
        this.rewardCredits = rewardCredits;
        this.cannonShots = cannonShots;
    }

    /**
     * Grants the reward credits to the specified player upon successful defense.
     *
     * @param player the player receiving the reward
     */
    @Override
    protected void giveReward(Player player) {
        verifyInputState(NONE);
        player.giveCredits(rewardCredits);
        setInputState(FINISHED);
    }

    /**
     * Applies punishment to a player who failed the combat by initiating cannon shots against them.
     *
     * @param player the player being punished
     */
    @Override
    protected void applyPunishment(Player player) {
        verifyInputState(NONE);
        this.punishedPlayer = player;
        this.shotQueue = new LinkedList<>(Arrays.asList(cannonShots));
        applyShot();
    }

    private void applyShot() {
        verifyInputState(NONE);
        if(shotQueue.isEmpty()) {
            handleNextPlayer();
            return;
        }
        this.currentShot = shotQueue.getFirst();

        int[] dice = currentShot.randomizePathIndex();
        dispatcher.fireEvent(new DiceThrowEvent(punishedPlayer.getName(), dice));

        boolean isPlayerAtRisk = currentShot.evaluateDefense(punishedPlayer);

        if(isPlayerAtRisk) {
            setInputState(BATTERY_DECISION);
            dispatcher.fireEvent(new BatteryDecisionEvent(punishedPlayer, currentShot));
        }
        else handleNextShot();
    }

    private void handleNextShot() {
        shotQueue.removeFirst();
        applyShot();
    }

    /**
     * Called when the punished player chooses to use a battery to defend against the current cannon shot.
     *
     * @param caller        the player using the battery (must be the punished player)
     * @param batteryCoords the coordinates of the battery tile being used
     */
    @Override
    public void useBattery(Player caller, Point batteryCoords) {
        verifyInputState(BATTERY_DECISION);
        verifyCaller(caller, punishedPlayer);

        dispatcher.fireEvent(new DecisionEndEvent(caller.getName()));

        caller.ensureBatteryRemovedFrom(batteryCoords);

        dispatcher.fireEvent(new ProjectileEvent(caller, currentShot, null, true));

        setInputState(NONE);
        handleNextShot();
    }
    /**
     * Called when the punished player refuses to use a battery and takes the hit from the current cannon shot.
     *
     * @param caller the player refusing to use a battery (must be the punished player)
     */
    @Override
    public void refuseBattery(Player caller) {
        verifyInputState(BATTERY_DECISION);
        verifyCaller(caller, punishedPlayer);

        dispatcher.fireEvent(new DecisionEndEvent(caller.getName()));

        caller.applyProjectile(currentShot);

        setInputState(NONE);
        handleNextShot();
    }

    @Override
    public String getCardType() {
        return "Pirates";
    }
}
