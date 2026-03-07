package io.github.java_lan_multiplayer.common.messages.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JoinResponseMessage implements GameMessage {

    public enum JoinStatus {
        SUCCESS, LOBBY_FULL, NAME_TAKEN, GAME_STARTED
    }
    private JoinStatus joinStatus;

    public JoinResponseMessage() {}

    public JoinResponseMessage(JoinStatus joinStatus) {
        this.joinStatus = joinStatus;
    }

    public JoinStatus getJoinStatus() {
        return joinStatus;
    }

    @Override
    public String getType() {
        return "join_response";
    }
}
