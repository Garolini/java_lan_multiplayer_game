package io.github.java_lan_multiplayer.server.events.flight;

import io.github.java_lan_multiplayer.common.messages.flight.TileInfo;
import io.github.java_lan_multiplayer.server.events.EventModel;

import java.util.Set;

public class ShipUpdateEvent extends EventModel {

    private final String playerName;
    private final Set<TileInfo> tiles;

    public ShipUpdateEvent(String playerName, Set<TileInfo> tiles) {
        this.playerName = playerName;
        this.tiles = tiles;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Set<TileInfo> getTiles() {
        return tiles;
    }
}

