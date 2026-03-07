package io.github.java_lan_multiplayer.common.messages.shipBuilding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimerFlippedMessage implements GameMessage {

    public TimerFlippedMessage() {}

    @Override
    public String getType() {
        return "timer_flipped";
    }
}
