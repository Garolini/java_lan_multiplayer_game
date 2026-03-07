package io.github.java_lan_multiplayer.common.messages.shipBuilding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TileReturnedMessage implements GameMessage {

    private int tileId;

    public TileReturnedMessage() {}

    public TileReturnedMessage(int tileId) {
       this.tileId = tileId;
    }

    public int getTileId() {
        return tileId;
    }

    @Override
    public String getType() {
        return "tile_returned";
    }
}
