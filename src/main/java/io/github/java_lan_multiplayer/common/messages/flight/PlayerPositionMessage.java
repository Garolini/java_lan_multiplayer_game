package io.github.java_lan_multiplayer.common.messages.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerPositionMessage implements GameMessage {

    private String playerName;
    private int newPosition;

    public PlayerPositionMessage() {}

    public PlayerPositionMessage(String playerName, int newPosition) {
        this.playerName = playerName;
        this.newPosition = newPosition;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getNewPosition() {
        return newPosition;
    }

    @Override
    public String getType() {
        return "player_position";
    }
}
