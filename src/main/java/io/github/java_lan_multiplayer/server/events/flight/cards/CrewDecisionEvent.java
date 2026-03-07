package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.ShipBoard;

import java.util.Set;

public class CrewDecisionEvent extends EventModel {

    private final String playerName;
    private final Set<ShipBoard.CrewData> crewTiles;

    public CrewDecisionEvent(String playerName, Set<ShipBoard.CrewData> crewTiles) {
        this.playerName = playerName;
        this.crewTiles = crewTiles;
    }

    public String getPlayerName() {
        return playerName;
    }
    public Set<ShipBoard.CrewData> getCrewTiles() {
        return crewTiles;
    }
}
