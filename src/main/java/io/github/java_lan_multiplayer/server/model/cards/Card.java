package io.github.java_lan_multiplayer.server.model.cards;

import io.github.java_lan_multiplayer.server.events.EventDispatcher;
import io.github.java_lan_multiplayer.server.events.flight.cards.CardFinishedEvent;
import io.github.java_lan_multiplayer.server.model.GameModel;
import io.github.java_lan_multiplayer.server.model.Player;

import java.util.Collection;
import java.util.List;

/**
 * Abstract base class representing a card used in the game.
 * <p>
 * Each card has a unique identifier, a level (1 or 2), and can be activated within the context
 * of a {@link GameModel}. Once activated, it can interact with players and fire events using
 * the shared {@link EventDispatcher}.
 * <p>
 * Subclasses must implement the {@link #onActivate()} method and define their own behavior.
 */
public abstract class Card {

    private final int id;
    private final int level;
    protected GameModel board;

    /**
     * Enum representing the different input states a card might require during its lifecycle.
     */
    public enum InputState {
        NONE,
        PLANET_SELECTION,
        CANNON_SELECTION,
        ENGINE_SELECTION,
        CARGO_DECISION,
        BATTERY_DECISION,
        REWARD_DECISION,
        CREW_DECISION,
        FINISHED
    }
    private InputState inputState = InputState.NONE;

    protected EventDispatcher dispatcher;

    /**
     * Constructs a new Card with the specified ID and level.
     *
     * @param id    the unique identifier of the card.
     * @param level the level of the card (must be 1 or 2).
     * @throws IllegalArgumentException if the level is not between 1 and 2.
     */
    public Card(int id, int level) {
        this.id = id;
        if (level < 1 || level > 2) throw new IllegalArgumentException("Card level must be between 1 and 2.");
        this.level = level;
    }

    /**
     * Returns the unique identifier of this card.
     *
     * @return the card ID.
     */
    public int getId() {
        return id;
    }
    /**
     * Returns the level of this card.
     *
     * @return the card level (1 or 2).
     */
    public int getLevel() {
        return level;
    }

    /**
     * Activates the card, binding it to the specified game board.
     * This should only be called once per card instance.
     *
     * @param board the game model (board) to bind to this card.
     * @throws IllegalArgumentException if the board is null.
     * @throws IllegalStateException    if the card has already been activated.
     */
    public final void activate(GameModel board) {
        if(board == null) throw new IllegalArgumentException("Invalid board.");
        if(this.board != null) throw new IllegalStateException("Card has already been activated.");
        this.board = board;
        this.dispatcher = board.getEventDispatcher();
        onActivate();
    }
    /**
     * Abstract method to define card-specific behavior upon activation.
     * Must be implemented by subclasses.
     */
    protected abstract void onActivate();

    /**
     * Sets the input state of the card. If the state is set to {@code FINISHED},
     * it triggers an update of player status and dispatches a {@link CardFinishedEvent}.
     *
     * @param state the new input state.
     */
    protected final void setInputState(InputState state) {
        this.inputState = state;

        if(state == InputState.FINISHED) {
            board.updatePlayersStatus();
            dispatcher.fireEvent(new CardFinishedEvent());
        }
    }

    /**
     * Verifies that the given player is the first in the specified queue.
     *
     * @param caller       the player making the call.
     * @param playerQueue  the list of players in turn order.
     * @throws IllegalStateException if it's not the caller's turn or the queue is empty.
     */
    protected final void verifyPlayerTurn(Player caller, List<Player> playerQueue) {
        if(playerQueue.isEmpty()) throw new IllegalStateException("No players in queue.");
        Player currentPlayer = playerQueue.getFirst();
        verifyCaller(caller, currentPlayer);
    }

    /**
     * Verifies that the caller is the expected player.
     *
     * @param caller         the player making the call.
     * @param expectedPlayer the player expected to act.
     * @throws IllegalStateException if the caller is not the expected player.
     */
    protected final void verifyCaller(Player caller, Player expectedPlayer) {
        if(expectedPlayer != caller) {
            throw new IllegalStateException("It is not " + caller.getName() + "'s turn. It is currently " + expectedPlayer.getName() + "'s turn.");
        }
    }

    /**
     * Verifies that the caller is present in a list of allowed players.
     *
     * @param caller      the player making the call.
     * @param playerList  the collection of valid players.
     * @param message     the error message to include if the player is not found.
     * @throws IllegalStateException if the caller is not in the list.
     */
    protected final void verifyPlayerInList(Player caller, Collection<Player> playerList, String message) {
        if(!playerList.contains(caller)) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Verifies that the card is in the expected input state.
     *
     * @param expectedState the input state expected before continuing.
     * @throws IllegalStateException if the card is in an unexpected input state.
     */
    protected final void verifyInputState(InputState expectedState) {
        if(inputState != expectedState) {
            if(inputState == InputState.FINISHED) {
                throw new IllegalStateException("Card has already finished. No further input is expected.");
            }
            throw new IllegalStateException("Card is currently waiting for input: " + inputState + ". It was called a method that requires: " + expectedState + ".");
        }
    }

    /**
     * Returns whether the card has finished all required input or actions.
     *
     * @return {@code true} if the card is finished, {@code false} otherwise.
     */
    public final boolean isFinished() {
        return inputState == InputState.FINISHED;
    }

    /**
     * Returns the string representation of the card type.
     * Must be implemented by subclasses.
     *
     * @return the card type as a string.
     */
    public abstract String getCardType();
}