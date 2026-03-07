package io.github.java_lan_multiplayer.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.server.model.BoardType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BoardTypeMessage implements GameMessage {

    private BoardType boardType;

    public BoardTypeMessage() {}

    public BoardTypeMessage(BoardType boardType) {
       this.boardType = boardType;
    }

    public BoardType getBoardType() {
       return boardType;
    }

    @Override
    public String getType() {
        return "board_type";
    }
}
