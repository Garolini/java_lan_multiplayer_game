package io.github.java_lan_multiplayer.server.events.flight;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class PlayerPositionUpdateEvent extends EventModel {

    private final String playerName;
    private final int newPosition;

    public PlayerPositionUpdateEvent(String playerName, int newPosition) {
        this.playerName = playerName;
        this.newPosition = newPosition;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getNewPosition() {
        return newPosition;
    }
}

