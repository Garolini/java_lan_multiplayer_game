package io.github.java_lan_multiplayer.common.messages.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipBoardMessage implements GameMessage {

    private String playerName;
    private Set<TileInfo> tiles;

    public ShipBoardMessage() {}

    public ShipBoardMessage(String playerName, Set<TileInfo> tiles) {
        this.playerName = playerName;
        this.tiles = tiles;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Set<TileInfo> getTiles() {
        return tiles;
    }

    @Override
    public String getType() {
        return "ship_board";
    }
}
