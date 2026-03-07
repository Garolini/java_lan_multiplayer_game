package io.github.java_lan_multiplayer.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimplePlayerMessage implements GameMessage {

    private String type;

    private String playerName;

    public SimplePlayerMessage() {}

    public SimplePlayerMessage(String type, String playerName) {
        this.type = type;
        this.playerName = playerName;
    }

    public String getPlayerName() {
       return playerName;
    }

    @Override
    public String getType() {
        return type;
    }
}
