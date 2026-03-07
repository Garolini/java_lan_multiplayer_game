package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.Player;

public class PlanetSelectionEvent extends EventModel {

    private final String playerName;
    private final Player[] planetChosenBy;

    public PlanetSelectionEvent(String playerName, Player[] planetChosenBy) {
        this.playerName = playerName;
        this.planetChosenBy = planetChosenBy;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Player[] getPlanetChosenBy() {
        return planetChosenBy;
    }
}
