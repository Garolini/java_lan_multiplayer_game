package io.github.java_lan_multiplayer.server.model.cards;

import io.github.java_lan_multiplayer.server.model.Player;

/**
 * Interface for cards that present a binary decision to the player, such as accepting or refusing a reward or challenge.
 */
public interface DecisionHandlerCard {

    /**
     * Called when the player chooses to accept the decision offered by the card.
     *
     * @param caller the player accepting the decision
     */
    void accept(Player caller);

    /**
     * Called when the player refuses the decision offered by the card.
     *
     * @param caller the player refusing the decision
     */
    void refuse(Player caller);
}