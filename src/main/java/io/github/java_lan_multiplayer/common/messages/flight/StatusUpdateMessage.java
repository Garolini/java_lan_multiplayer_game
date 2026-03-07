package io.github.java_lan_multiplayer.common.messages.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusUpdateMessage implements GameMessage {

    private String type;

    private String status;
    private String playerName;

    public StatusUpdateMessage() {}

    public StatusUpdateMessage(String type, String status, String playerName) {
        this.type = type;
        this.status = status;
        this.playerName = playerName;
    }

    public String getStatus() {
        return status;
    }

    public String getPlayerName() {
       return playerName;
    }

    @Override
    public String getType() {
        return type;
    }
}
