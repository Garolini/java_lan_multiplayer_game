package io.github.java_lan_multiplayer.server.events.shipBuilding;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class DoneBuildingEvent extends EventModel {

    private final String playerName;
    private final int position;

    public DoneBuildingEvent(String playerName, int position) {
        this.playerName = playerName;
        this.position = position;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPosition() {
        return position;
    }
}

