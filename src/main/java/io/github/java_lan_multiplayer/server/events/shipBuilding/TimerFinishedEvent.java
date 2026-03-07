package io.github.java_lan_multiplayer.server.events.shipBuilding;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class TimerFinishedEvent extends EventModel {

    private final boolean finalFlip;

    public TimerFinishedEvent(boolean finalFlip) {
        this.finalFlip = finalFlip;
    }

    public boolean isFinalFlip() {
        return finalFlip;
    }
}
