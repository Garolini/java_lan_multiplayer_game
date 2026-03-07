package io.github.java_lan_multiplayer.server.events.shipBuilding;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class TimerStartEvent extends EventModel {

    private final int duration;
    private final int timesFlipped;

    public TimerStartEvent(int duration, int timesFlipped) {
        this.duration = duration;
        this.timesFlipped = timesFlipped;
    }

    public int getDuration() {
        return duration;
    }

    public int getTimesFlipped() {
        return timesFlipped;
    }
}
