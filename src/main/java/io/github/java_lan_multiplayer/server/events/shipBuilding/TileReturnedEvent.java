package io.github.java_lan_multiplayer.server.events.shipBuilding;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class TileReturnedEvent extends EventModel {

    private final int tileId;

    public TileReturnedEvent(int tileId) {
        this.tileId = tileId;
    }

    public int getTileId() {
        return tileId;
    }
}
