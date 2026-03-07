package io.github.java_lan_multiplayer.common.messages.lobby;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BoardSelectMessage implements GameMessage {

    public enum Action {
        PREVIOUS, NEXT
    }
    private Action action;

    public BoardSelectMessage() {}

    public BoardSelectMessage(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public String getType() {
        return "board_select";
    }
}
