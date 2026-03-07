package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class DecisionEndEvent extends EventModel {

    private final String playerName;

    public DecisionEndEvent(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }
}
