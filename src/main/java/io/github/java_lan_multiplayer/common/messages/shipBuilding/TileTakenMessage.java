package io.github.java_lan_multiplayer.common.messages.shipBuilding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TileTakenMessage implements GameMessage {

    private String playerName;
    private int tileId;

    public TileTakenMessage() {}

    public TileTakenMessage(String playerName, int tileId) {
       this.playerName = playerName;
       this.tileId = tileId;
    }

    public String getPlayerName() {
       return playerName;
    }

    public int getTileId() {
        return tileId;
    }

    @Override
    public String getType() {
        return "tile_taken";
    }
}
