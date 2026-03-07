package io.github.java_lan_multiplayer.server.events.flight;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.ShipBoard;

import java.util.Set;

public class BatteriesUpdateEvent extends EventModel {

    private final String playerName;
    private final Set<ShipBoard.BatteriesData> batteries;

    public BatteriesUpdateEvent(String playerName, Set<ShipBoard.BatteriesData> batteries) {
        this.playerName = playerName;
        this.batteries = batteries;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Set<ShipBoard.BatteriesData> getBatteries() {
        return batteries;
    }
}

