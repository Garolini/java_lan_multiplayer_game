package io.github.java_lan_multiplayer.common.messages.shipBuilding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimerStartMessage implements GameMessage {

    private int duration;
    private int timesFlipped;

    public TimerStartMessage() {}

    public TimerStartMessage(int duration, int timesFlipped) {
       this.duration = duration;
       this.timesFlipped = timesFlipped;
    }

    public int getDuration() {
       return duration;
    }

    public int getTimesFlipped() {
        return timesFlipped;
    }

    @Override
    public String getType() {
        return "timer_start";
    }
}
