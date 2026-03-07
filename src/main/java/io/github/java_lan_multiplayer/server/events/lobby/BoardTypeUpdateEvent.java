package io.github.java_lan_multiplayer.server.events.lobby;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.BoardType;

public class BoardTypeUpdateEvent extends EventModel {

    private final BoardType boardType;

    public BoardTypeUpdateEvent(BoardType boardType) {
        this.boardType = boardType;
    }

    public BoardType getBoardType() {
        return boardType;
    }
}
