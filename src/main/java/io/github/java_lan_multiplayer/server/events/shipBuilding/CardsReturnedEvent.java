package io.github.java_lan_multiplayer.server.events.shipBuilding;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class CardsReturnedEvent extends EventModel {

    private final int pileId;

    public CardsReturnedEvent(int pileId) {
        this.pileId = pileId;
    }

    public int getPileId() {
        return pileId;
    }
}
