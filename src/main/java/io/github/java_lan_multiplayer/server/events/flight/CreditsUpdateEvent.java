package io.github.java_lan_multiplayer.server.events.flight;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class CreditsUpdateEvent extends EventModel {

    private final String playerName;
    private final int credits;

    public CreditsUpdateEvent(String playerName, int credits) {
        this.playerName = playerName;
        this.credits = credits;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getCredits() {
        return credits;
    }
}

