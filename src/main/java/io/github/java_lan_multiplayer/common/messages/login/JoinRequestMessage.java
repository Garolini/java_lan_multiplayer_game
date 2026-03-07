package io.github.java_lan_multiplayer.common.messages.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JoinRequestMessage implements GameMessage {

    private PlayerInfo playerInfo;

    public JoinRequestMessage() {}

    public JoinRequestMessage(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    @Override
    public String getType() {
        return "join_request";
    }
}
