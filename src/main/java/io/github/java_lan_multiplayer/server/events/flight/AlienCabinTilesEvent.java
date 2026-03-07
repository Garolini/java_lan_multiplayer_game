package io.github.java_lan_multiplayer.server.events.flight;

import io.github.java_lan_multiplayer.server.events.EventModel;

import java.awt.*;
import java.util.Set;

public class AlienCabinTilesEvent extends EventModel {

    private final String playerName;
    private final Set<Point> cabinTiles;

    public AlienCabinTilesEvent(String playerName, Set<Point> cabinTiles) {
        this.playerName = playerName;
        this.cabinTiles = cabinTiles;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Set<Point> getCabinTiles() {
        return cabinTiles;
    }
}

