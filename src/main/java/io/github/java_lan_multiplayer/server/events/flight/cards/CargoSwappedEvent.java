package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.Player;

import java.awt.*;

public class CargoSwappedEvent extends EventModel {

    private final Player player;
    private final int blockIndex;
    private final Point tileCoords;
    private final int containerIndex;

    public CargoSwappedEvent(Player player, int blockIndex, Point tileCoords, int containerIndex) {
        this.player = player;
        this.blockIndex = blockIndex;
        this.tileCoords = tileCoords;
        this.containerIndex = containerIndex;
    }

    public Player getPlayer() {
        return player;
    }
    public int getBlockIndex() {
        return blockIndex;
    }
    public Point getTileCoords() {
        return tileCoords;
    }
    public int getContainerIndex() {
        return containerIndex;
    }
}
