package io.github.java_lan_multiplayer.server.model.cards;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.server.events.flight.cards.*;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.projectile.CannonShot;
import io.github.java_lan_multiplayer.server.model.tiles.CannonTile;
import io.github.java_lan_multiplayer.server.model.tiles.EngineTile;

import java.awt.*;
import java.util.*;
import java.util.List;

import static io.github.java_lan_multiplayer.server.model.cards.Card.InputState.*;

/**
 * Represents a combat zone card that imposes various challenges on players,
 * such as comparing crew size, engine power, and cannon power to determine penalties.
 * Penalties include flight day loss, crew loss, cargo loss, or cannon shots.
 * <p>
 * Implements handlers for engines, cannons, batteries, and crew decisions during combat phases.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class CombatZoneCard extends Card implements EnginesHandlerCard, CannonsHandlerCard, BatteryHandlerCard, CrewHandlerCard {

    /**
     * A combat rule defining a condition to evaluate players and a punishment
     * to apply to the player with the worst result.
     */
    public static class CombatRule {
        public enum Condition {CREW_SIZE, CANNON_POWER, ENGINE_POWER}

        public enum Punishment {FLIGHT_DAYS, CREW_LOSS, CARGO_LOSS, CANNON_SHOTS}

        private final Condition condition;
        private final Punishment punishment;
        private final int punishmentValue;
        private final CannonShot[] cannonShots;

        /**
         * Constructs a CombatRule with the specified condition and punishment.
         *
         * @param condition the condition to evaluate players by
         * @param punishment the punishment to apply to the worst player
         * @param punishmentValue the numeric value for the punishment (ignored for CANNON_SHOTS)
         * @param cannonShots the array of cannon shots (used only if punishment is CANNON_SHOTS)
         * @throws IllegalArgumentException if punishmentValue is negative for non-CANNON_SHOTS punishment
         */
        @JsonCreator
        public CombatRule(
                @JsonProperty("condition") Condition condition,
                @JsonProperty("punishment") Punishment punishment,
                @JsonProperty("punishmentValue") int punishmentValue,
                @JsonProperty("cannonShots") CannonShot[] cannonShots) {
            this.condition = condition;
            this.punishment = punishment;
            if (punishment != Punishment.CANNON_SHOTS) {
                if (punishmentValue < 0) throw new IllegalArgumentException("punishment value must be >= 0");
                this.punishmentValue = punishmentValue;
            } else this.punishmentValue = 0;
            if (punishment == Punishment.CANNON_SHOTS) this.cannonShots = cannonShots;
            else this.cannonShots = null;
        }
        public Condition getCondition() {
            return condition;
        }
        public Punishment getPunishment() {
            return punishment;
        }
        public int getPunishmentValue() {
            return punishmentValue;
        }
        public CannonShot[] getCannonShots() {
            return cannonShots;
        }
    }

    private final CombatRule[] combatRules;

    private List<CombatRule> ruleQueue;
    private CombatRule currentRule;

    private List<Player> playerQueue;
    private Map<Player, Double> playerScores;

    private int remainingCrewToGiveUp;

    private List<CannonShot> shotQueue;
    private Player punishedPlayer;
    private CannonShot currentShot;

    public CombatZoneCard(
            @JsonProperty("id") int id,
            @JsonProperty("level") int level,
            @JsonProperty("combatRules") CombatRule[] combatRules) {
        super(id, level);
        this.combatRules = combatRules;
    }

    /**
     * Activates the card and initializes the rule processing sequence.
     * Starts evaluating combat rules in order.
     */
    @Override
    protected void onActivate() {
        this.ruleQueue = new LinkedList<>(Arrays.asList(combatRules));
        setInputState(NONE);
        applyRule();
    }

    private void applyRule() {
        verifyInputState(NONE);
        if(ruleQueue.isEmpty()) {
            setInputState(FINISHED);
            return;
        }
        this.currentRule = ruleQueue.getFirst();
        List<Player> alivePlayers = board.getSortedPlayers();

        if(alivePlayers.size() < 2) {
            dispatcher.fireEvent(new CardStatusEvent(CardStatusEvent.CardStatus.COMBAT_SKIPPED));
            setInputState(FINISHED);
            return;
        }

        this.playerScores = new HashMap<>();

        switch(currentRule.getCondition()) {
            case CREW_SIZE -> {
                dispatcher.fireEvent(new CardStatusEvent(CardStatusEvent.CardStatus.RULE_CREW));
                for(Player player : alivePlayers) {
                    double score = player.getCrewCount();
                    dispatcher.fireEvent(new PlayerScoreEvent(player.getName(), "crew", score));
                    playerScores.put(player,score);
                }
                applyPunishment();
            }
            case ENGINE_POWER -> {
                dispatcher.fireEvent(new CardStatusEvent(CardStatusEvent.CardStatus.RULE_ENGINE));
                this.playerQueue = alivePlayers;

                setInputState(ENGINE_SELECTION);
                dispatcher.fireEvent(new EngineSelectionEvent(playerQueue.getFirst(), false));
            }
            case CANNON_POWER -> {
                dispatcher.fireEvent(new CardStatusEvent(CardStatusEvent.CardStatus.RULE_CANNON));
                this.playerQueue = alivePlayers;

                setInputState(CANNON_SELECTION);
                dispatcher.fireEvent(new CannonSelectionEvent(playerQueue.getFirst()));
            }
        }
    }

    private void handleNextRule() {
        ruleQueue.removeFirst();
        applyRule();
    }

    /**
     * Handles a player's engine activation, records their engine strength score,
     * and proceeds to the next player or applies the punishment.
     *
     * @param caller the player activating their engines
     * @param activatedEngines number of engines activated
     * @param batteryTiles battery tiles used for engine activation
     * @throws IllegalArgumentException if the number of batteries doesn't match activated engines
     */
    @Override
    public void activateEngines(Player caller, int activatedEngines, List<Point> batteryTiles) {
        verifyInputState(ENGINE_SELECTION);
        verifyPlayerTurn(caller, playerQueue);
        dispatcher.fireEvent(new DecisionEndEvent(caller.getName()));
        if(activatedEngines != batteryTiles.size()) throw new IllegalArgumentException("The number of activated engines does not match the number of batteries used.");
        double strength = caller.getStrength(EngineTile.class, activatedEngines);
        caller.removeBatteries(batteryTiles);
        playerScores.put(caller, strength);
        dispatcher.fireEvent(new PlayerScoreEvent(caller.getName(), "engines", strength));

        playerQueue.removeFirst();

        if (!playerQueue.isEmpty()) {
            dispatcher.fireEvent(new EngineSelectionEvent(playerQueue.getFirst(), false));
        } else {
            setInputState(NONE);
            applyPunishment();
        }
    }
    /**
     * Handles a player's cannon activation, records their cannon power score,
     * and proceeds to the next player or applies the punishment.
     *
     * @param caller the player activating their cannons
     * @param verticalCannons number of vertical cannons
     * @param rotatedCannons number of rotated cannons
     * @param batteryTiles battery tiles used for cannon activation
     * @throws IllegalArgumentException if the number of batteries doesn't match cannon use
     */
    @Override
    public void activateCannons(Player caller, int verticalCannons, int rotatedCannons, List<Point> batteryTiles) {
        verifyInputState(CANNON_SELECTION);
        verifyPlayerTurn(caller, playerQueue);
        dispatcher.fireEvent(new DecisionEndEvent(caller.getName()));
        if(verticalCannons + rotatedCannons != batteryTiles.size()) throw new IllegalArgumentException("The number of activated cannons does not match the number of batteries used.");
        double strength = caller.getStrength(CannonTile.class, verticalCannons, rotatedCannons);
        caller.removeBatteries(batteryTiles);
        playerScores.put(caller, strength);
        dispatcher.fireEvent(new PlayerScoreEvent(caller.getName(), "cannons", strength));

        playerQueue.removeFirst();

        if (!playerQueue.isEmpty()) {
            dispatcher.fireEvent(new CannonSelectionEvent(playerQueue.getFirst()));
        } else {
            setInputState(NONE);
            applyPunishment();
        }
    }

    private void applyPunishment() {
        verifyInputState(NONE);

        Player worstPlayer = getWorstPlayer(board.getSortedPlayers());
        dispatcher.fireEvent(new PlayerStatusEvent(worstPlayer.getName(), PlayerStatusEvent.Status.WORST_PLAYER));

        switch(currentRule.getPunishment()) {
            case FLIGHT_DAYS -> {
                board.movePlayerBackward(worstPlayer, currentRule.getPunishmentValue());
                handleNextRule();
            }
            case CREW_LOSS -> {
                if(currentRule.getPunishmentValue() <= 0) break;
                this.punishedPlayer = worstPlayer;
                this.remainingCrewToGiveUp = currentRule.getPunishmentValue();
                setInputState(CREW_DECISION);
                dispatcher.fireEvent(new CrewDecisionEvent(worstPlayer.getName(), worstPlayer.getShipContent().crewData()));
            }
            case CARGO_LOSS -> {
                worstPlayer.removeMostValuableCargo(currentRule.getPunishmentValue());
                handleNextRule();
            }
            case CANNON_SHOTS -> {
                this.punishedPlayer = worstPlayer;
                this.shotQueue = new LinkedList<>(Arrays.asList(currentRule.getCannonShots()));
                applyShot();
            }
        }
    }

    /**
     * Handles the removal of a crew member from a specific cabin during a crew loss punishment.
     *
     * @param caller the player losing crew
     * @param cabinCoords coordinates of the cabin from which to remove crew
     * @throws RuntimeException if no more crew are required to be removed
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
            handleNextRule();
        }
    }

    private void applyShot() {
        verifyInputState(NONE);
        if(shotQueue.isEmpty()) {
            handleNextRule();
            return;
        }
        this.currentShot = shotQueue.getFirst();

        int[] dice = currentShot.randomizePathIndex();
        dispatcher.fireEvent(new DiceThrowEvent(punishedPlayer.getName(), dice));

        boolean isPlayerAtRisk = currentShot.evaluateDefense(punishedPlayer);

        if(isPlayerAtRisk) {
            setInputState(BATTERY_DECISION);
            dispatcher.fireEvent(new BatteryDecisionEvent(punishedPlayer, currentShot));
        } else {
            handleNextShot();
        }
    }

    private void handleNextShot() {
        shotQueue.removeFirst();
        applyShot();
    }

    /**
     * Called when a player decides to use a battery to defend against a cannon shot.
     *
     * @param caller the player using a battery
     * @param batteryCoords the coordinates of the battery tile used
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
     * Called when a player refuses to use a battery to defend against a cannon shot.
     *
     * @param caller the player refusing battery use
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

    private Player getWorstPlayer(List<Player> players) {

        if(players.isEmpty()) throw new IllegalArgumentException("Players list cannot be empty.");

        Player worstPlayer = players.getFirst();
        if(!playerScores.containsKey(worstPlayer)) throw new RuntimeException("Player " + worstPlayer.getName() + " score was not found.");
        double worstScore = playerScores.get(worstPlayer);

        for(Player player : players) {
            if(!playerScores.containsKey(player)) throw new RuntimeException("Player " + player.getName() + " score was not found.");
            double currentScore = playerScores.get(player);

            if(currentScore < worstScore) {
                worstPlayer = player;
                worstScore = currentScore;
            }
        }
        return worstPlayer;
    }

    @Override
    public String getCardType() {
        return "Combat Zone";
    }
}
