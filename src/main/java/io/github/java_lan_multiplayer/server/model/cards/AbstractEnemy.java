package io.github.java_lan_multiplayer.server.model.cards;

import io.github.java_lan_multiplayer.server.events.flight.cards.CannonSelectionEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.CardDecisionEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.DecisionEndEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.PlayerStatusEvent;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.tiles.CannonTile;

import java.awt.*;
import java.util.List;

import static io.github.java_lan_multiplayer.server.model.cards.Card.InputState.*;

/**
 * Abstract base class for enemy encounter cards. These cards challenge players
 * to meet a required cannon power threshold. Depending on the player's cannon strength,
 * they may receive a reward, suffer a punishment, or have to make a decision.
 *
 * <p>This card handles player turns sequentially and transitions between states:
 * <ul>
 *     <li><b>Punishment</b> if the max cannon power is insufficient</li>
 *     <li><b>Reward decision</b> if the minimum cannon power is more than sufficient</li>
 *     <li><b>Cannon selection</b> if power is borderline and player must choose configuration</li>
 * </ul>
 * Subclasses must define how rewards and punishments are applied.
 */
public abstract class AbstractEnemy extends Card implements CannonsHandlerCard, DecisionHandlerCard {

    private final int requiredCannonPower;
    private final int positionCost;

    private List<Player> playerQueue;

    public AbstractEnemy(int id, int level, int requiredCannonPower, int positionCost) {
        super(id, level);
        if (requiredCannonPower < 0) throw new IllegalArgumentException("requiredCannonPower must be a positive number.");
        this.requiredCannonPower = requiredCannonPower;
        if (positionCost < 0) throw new IllegalArgumentException("positionCost must be a positive number.");
        this.positionCost = positionCost;
    }

    /**
     * Applies a subclass-defined reward to the given player.
     *
     * @param player the rewarded player
     */
    protected abstract void giveReward(Player player);
    /**
     * Applies a subclass-defined punishment to the given player.
     *
     * @param player the punished player
     */
    protected abstract void applyPunishment(Player player);

    /**
     * Activates the card and starts processing players.
     */
    @Override
    protected void onActivate() {
        playerQueue = board.getSortedPlayers();
        handlePlayer();
    }

    /**
     * Handles the logic for the current player in the queue.
     * Decides whether they are automatically rewarded/punished or must select cannons.
     */
    private void handlePlayer() {
        verifyInputState(NONE);
        if(playerQueue.isEmpty()) {
            setInputState(FINISHED);
            return;
        }
        Player currentPlayer = playerQueue.getFirst();

        if(currentPlayer.getMaxStrength(CannonTile.class) < requiredCannonPower) {
            dispatcher.fireEvent(new PlayerStatusEvent(currentPlayer.getName(), PlayerStatusEvent.Status.PUNISHED));
            applyPunishment(currentPlayer);
        }
        else if(currentPlayer.getMinStrength(CannonTile.class) > requiredCannonPower) {
            setInputState(REWARD_DECISION);
            dispatcher.fireEvent(new PlayerStatusEvent(currentPlayer.getName(), PlayerStatusEvent.Status.REWARDED));
            dispatcher.fireEvent(new CardDecisionEvent(currentPlayer.getName()));
        }
        else {
            setInputState(CANNON_SELECTION);
            dispatcher.fireEvent(new CannonSelectionEvent(currentPlayer));
        }
    }

    /**
     * Proceeds to the next player in the queue.
     */
    protected void handleNextPlayer() {
        playerQueue.removeFirst();
        setInputState(NONE);
        handlePlayer();
    }

    /**
     * Handles a player's cannon selection and battery usage, then evaluates the outcome.
     *
     * @param caller          the player making the selection
     * @param verticalCannons count of vertical cannons used
     * @param rotatedCannons  count of rotated cannons used
     * @param batteryTiles    list of battery tiles used
     */
    @Override
    public void activateCannons(Player caller, int verticalCannons, int rotatedCannons, List<Point> batteryTiles) {
        verifyInputState(CANNON_SELECTION);
        verifyPlayerTurn(caller, playerQueue);
        dispatcher.fireEvent(new DecisionEndEvent(caller.getName()));
        if(verticalCannons + rotatedCannons != batteryTiles.size()) throw new IllegalArgumentException("The number of activated cannons does not match the number of batteries used.");
        double strength = caller.getStrength(CannonTile.class, verticalCannons, rotatedCannons);
        caller.removeBatteries(batteryTiles);
        if(strength > requiredCannonPower) {
            setInputState(REWARD_DECISION);

            dispatcher.fireEvent(new PlayerStatusEvent(caller.getName(), PlayerStatusEvent.Status.REWARDED));
            dispatcher.fireEvent(new CardDecisionEvent(caller.getName()));
        } else if (strength < requiredCannonPower) {
            setInputState(NONE);
            dispatcher.fireEvent(new PlayerStatusEvent(caller.getName(), PlayerStatusEvent.Status.PUNISHED));
            applyPunishment(caller);
        } else {
            dispatcher.fireEvent(new PlayerStatusEvent(caller.getName(), PlayerStatusEvent.Status.SKIPPED_ENEMY));
            handleNextPlayer();
        }
    }

    /**
     * Called when the player accepts the reward.
     * Applies the cost, gives reward, and ends the encounter.
     *
     * @param caller the accepting player
     */
    @Override
    public void accept(Player caller) {
        verifyInputState(REWARD_DECISION);
        verifyPlayerTurn(caller, playerQueue);
        board.movePlayerBackward(caller, positionCost);
        playerQueue.clear();
        setInputState(NONE);
        giveReward(caller);
    }
    /**
     * Called when the player refuses the reward.
     * Ends the card processing for all players.
     *
     * @param caller the refusing player
     */
    @Override
    public void refuse(Player caller) {
        verifyInputState(REWARD_DECISION);
        verifyPlayerTurn(caller, playerQueue);
        playerQueue.clear();
        setInputState(FINISHED);
    }
}
