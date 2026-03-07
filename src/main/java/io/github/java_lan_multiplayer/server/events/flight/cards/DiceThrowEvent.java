package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class DiceThrowEvent extends EventModel {

    private final String playerName;
    private final int[] dice;

    public DiceThrowEvent(String playerName, int[] dice) {
        this.playerName = playerName;
        this.dice = dice;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int[] getDice() {
        return dice;
    }
}
