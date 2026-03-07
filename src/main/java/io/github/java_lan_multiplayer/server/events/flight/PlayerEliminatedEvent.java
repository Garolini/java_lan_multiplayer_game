package io.github.java_lan_multiplayer.server.events.flight;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class PlayerEliminatedEvent extends EventModel {

    private final String playerName;

    public PlayerEliminatedEvent(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }
}

