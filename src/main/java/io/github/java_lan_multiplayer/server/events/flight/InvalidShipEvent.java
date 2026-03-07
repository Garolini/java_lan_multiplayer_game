package io.github.java_lan_multiplayer.server.events.flight;

import io.github.java_lan_multiplayer.server.events.EventModel;

import java.awt.*;
import java.util.Set;

public class InvalidShipEvent extends EventModel {

    private final String playerName;
    private final Set<Point> invalidTiles;

    public InvalidShipEvent(String playerName, Set<Point> invalidTiles) {
        this.playerName = playerName;
        this.invalidTiles = invalidTiles;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Set<Point> getInvalidTiles() {
        return invalidTiles;
    }
}

