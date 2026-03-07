package io.github.java_lan_multiplayer.common.messages.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

import java.awt.*;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TilesMessage implements GameMessage {

    private String playerName;
    private Set<Point> tiles;
    private String type;
    // "invalidTiles" or "alienCabinTiles"

    public TilesMessage() {}

    public TilesMessage(String type, String playerName, Set<Point> tiles) {
        this.type = type;
        this.playerName = playerName;
        this.tiles = tiles;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Set<Point> getTiles() {
        return tiles;
    }

    @Override
    public String getType() {
        return type;
    }
}
