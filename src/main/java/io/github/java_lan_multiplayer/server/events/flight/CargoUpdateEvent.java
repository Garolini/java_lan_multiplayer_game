package io.github.java_lan_multiplayer.server.events.flight;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.ShipBoard;

import java.util.Set;

public class CargoUpdateEvent extends EventModel {

    private final String playerName;
    private final Set<ShipBoard.CargoData> cargo;

    public CargoUpdateEvent(String playerName, Set<ShipBoard.CargoData> cargo) {
        this.playerName = playerName;
        this.cargo = cargo;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Set<ShipBoard.CargoData> getCargo() {
        return cargo;
    }
}

