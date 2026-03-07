package io.github.java_lan_multiplayer.server.events.flight;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.ShipBoard;

import java.util.Set;

public class CrewUpdateEvent extends EventModel {

    private final String playerName;
    private final Set<ShipBoard.CrewData> crew;

    public CrewUpdateEvent(String playerName, Set<ShipBoard.CrewData> crew) {
        this.playerName = playerName;
        this.crew = crew;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Set<ShipBoard.CrewData> getCrew() {
        return crew;
    }
}

