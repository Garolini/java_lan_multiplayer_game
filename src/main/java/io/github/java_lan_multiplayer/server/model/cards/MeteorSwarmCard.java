package io.github.java_lan_multiplayer.server.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.server.events.flight.cards.BatteryDecisionEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.DecisionEndEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.DiceThrowEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.ProjectileEvent;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.projectile.Meteor;

import java.awt.*;
import java.util.*;
import java.util.List;

import static io.github.java_lan_multiplayer.server.model.cards.Card.InputState.*;

/**
 * Represents a "Meteor Swarm" card that triggers a sequence of meteor strikes.
 * Players at risk may choose to use a battery to defend against incoming meteors.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class MeteorSwarmCard extends Card implements BatteryHandlerCard {

    private final Meteor[] meteors;

    private List<Meteor> meteorQueue;
    private Meteor currentMeteor;
    private Set<Player> playersAtRisk;

    public MeteorSwarmCard(
            @JsonProperty("id") int id,
            @JsonProperty("level") int level,
            @JsonProperty("meteors") Meteor[] meteors) {
        super(id, level);
        this.meteors = meteors;
    }

    /**
     * Activates the card, initializing and applying the first meteor in the sequence.
     */
    @Override
    protected void onActivate() {
        this.meteorQueue = new LinkedList<>(Arrays.asList(meteors));
        applyMeteor();
    }

    private void applyMeteor() {
        verifyInputState(NONE);
        if(meteorQueue.isEmpty()) {
            setInputState(FINISHED);
            return;
        }
        List<Player> players = board.getSortedPlayers();
        this.currentMeteor = meteorQueue.getFirst();

        int[] dice = currentMeteor.randomizePathIndex();
        dispatcher.fireEvent(new DiceThrowEvent(players.getFirst().getName(), dice));

        this.playersAtRisk = new HashSet<>();
        for(Player player : players) {
            if(currentMeteor.evaluateDefense(player)) {
                playersAtRisk.add(player);
            }
        }
        if(!playersAtRisk.isEmpty()) {
            setInputState(BATTERY_DECISION);

            for(Player player : playersAtRisk) {
                dispatcher.fireEvent(new BatteryDecisionEvent(player, currentMeteor));
            }
        }

        handleNextMeteorIfReady();
    }

    /**
     * Called when a player chooses to use a battery to defend against an incoming meteor.
     *
     * @param caller       the player using the battery
     * @param batteryCoords the coordinates of the battery being used
     */
    @Override
    public void useBattery(Player caller, Point batteryCoords) {
        verifyInputState(BATTERY_DECISION);
        verifyPlayerInList(caller, playersAtRisk, "Player " + caller.getName() + " is not currently at risk.");

        dispatcher.fireEvent(new DecisionEndEvent(caller.getName()));

        caller.ensureBatteryRemovedFrom(batteryCoords);

        dispatcher.fireEvent(new ProjectileEvent(caller, currentMeteor, null, true));
        playersAtRisk.remove(caller);

        handleNextMeteorIfReady();
    }
    /**
     * Called when a player refuses to use a battery and instead takes the hit from the meteor.
     *
     * @param caller the player refusing to use a battery
     */
    @Override
    public void refuseBattery(Player caller) {
        verifyInputState(BATTERY_DECISION);
        verifyPlayerInList(caller, playersAtRisk, "Player " + caller.getName() + " is not currently at risk.");

        dispatcher.fireEvent(new DecisionEndEvent(caller.getName()));

        caller.applyProjectile(currentMeteor);
        playersAtRisk.remove(caller);

        handleNextMeteorIfReady();
    }

    private void handleNextMeteorIfReady() {
        if(playersAtRisk.isEmpty()) {
            meteorQueue.removeFirst();
            setInputState(NONE);
            applyMeteor();
        }
    }

    @Override
    public String getCardType() {
        return "Meteor Swarm";
    }
}
