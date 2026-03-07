package io.github.java_lan_multiplayer.server.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.server.events.flight.cards.DecisionEndEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.CrewDecisionEvent;
import io.github.java_lan_multiplayer.server.model.Player;

import java.awt.*;

import static io.github.java_lan_multiplayer.server.model.cards.Card.InputState.*;

/**
 * Represents a Slavers enemy encounter card.
 * In this combat event, players must meet a required cannon power to avoid punishment.
 * If successful, the player is rewarded with credits. If defeated, the player must
 * give up a specified number of crew members.
 * <p>
 * Implements crew handling during punishment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class SlaversCard extends AbstractEnemy implements CrewHandlerCard {
    // Combat encounter, win to gain credits, lose to lose crew members.

    private final int rewardCredits;
    private final int crewLossOnDefeat;

    private Player punishedPlayer;
    private int remainingCrewToGiveUp;

    public SlaversCard(
            @JsonProperty("id") int id,
            @JsonProperty("level") int level,
            @JsonProperty("requiredCannonPower") int requiredCannonPower,
            @JsonProperty("rewardCredits") int rewardCredits,
            @JsonProperty("positionCost") int positionCost,
            @JsonProperty("crewLossOnDefeat") int crewLossOnDefeat) {
        super(id, level, requiredCannonPower, positionCost);
        if (rewardCredits < 0) throw new IllegalArgumentException("rewardCredits must be a positive number.");
        this.rewardCredits = rewardCredits;
        if (crewLossOnDefeat < 0) throw new IllegalArgumentException("crewLossOnDefeat must be a positive number.");
        this.crewLossOnDefeat = crewLossOnDefeat;
    }

    /**
     * Grants the reward to the player who succeeded in the encounter.
     *
     * @param player the player who won the combat
     */
    @Override
    protected void giveReward(Player player) {
        verifyInputState(NONE);
        player.giveCredits(rewardCredits);
        setInputState(FINISHED);
    }

    /**
     * Applies the punishment to a player who failed the encounter.
     * The player is required to give up a specified number of crew members.
     *
     * @param player the player who failed the combat
     */
    @Override
    protected void applyPunishment(Player player) {
        verifyInputState(NONE);
        if(crewLossOnDefeat > 0) {
            this.punishedPlayer = player;
            this.remainingCrewToGiveUp = crewLossOnDefeat;
            setInputState(CREW_DECISION);

            dispatcher.fireEvent(new CrewDecisionEvent(player.getName(), player.getShipContent().crewData()));
        }
        else handleNextPlayer();
    }

    /**
     * Removes a crew member from the specified cabin coordinate
     * as part of the punishment.
     *
     * @param caller      the player removing crew
     * @param cabinCoords the coordinates of the cabin where the crew is being removed
     * @throws RuntimeException if there are no more crew required to be removed
     */
    @Override
    public void removeMemberFrom(Player caller, Point cabinCoords) {
        verifyInputState(CREW_DECISION);
        verifyCaller(caller, punishedPlayer);
        if(remainingCrewToGiveUp <= 0) throw new RuntimeException("No more crew members are required to be removed.");
        caller.ensureCrewRemovedAt(cabinCoords);
        remainingCrewToGiveUp--;
        if(remainingCrewToGiveUp == 0 || punishedPlayer.getCrewCount() == 0) {
            dispatcher.fireEvent(new DecisionEndEvent(caller.getName()));
            setInputState(NONE);
            handleNextPlayer();
        }
    }

    @Override
    public String getCardType() {
        return "Slavers";
    }
}
