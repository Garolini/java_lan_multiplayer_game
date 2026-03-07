package io.github.java_lan_multiplayer.common.messages.shipBuilding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimerFinishedMessage implements GameMessage {

    private boolean finalFlip;

    public TimerFinishedMessage() {}

    public TimerFinishedMessage(boolean finalFlip) {
        this.finalFlip = finalFlip;
    }

    public boolean isFinalFlip() {
        return finalFlip;
    }

    @Override
    public String getType() {
        return "timer_finished";
    }
}
