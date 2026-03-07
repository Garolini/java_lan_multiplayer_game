package io.github.java_lan_multiplayer.server.events.game;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class GameResetEvent extends EventModel {
    private final String playerName;

    public GameResetEvent(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }
}
