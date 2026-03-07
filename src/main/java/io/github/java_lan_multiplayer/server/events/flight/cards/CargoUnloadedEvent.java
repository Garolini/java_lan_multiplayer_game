package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.Player;

import java.awt.*;

public class CargoUnloadedEvent extends EventModel {

    private final Player player;
    private final Point tileCoords;
    private final int containerIndex;

    public CargoUnloadedEvent(Player player, Point tileCoords, int containerIndex) {
        this.player = player;
        this.tileCoords = tileCoords;
        this.containerIndex = containerIndex;
    }

    public Player getPlayer() {
        return player;
    }
    public Point getTileCoords() {
        return tileCoords;
    }
    public int getContainerIndex() {
        return containerIndex;
    }
}
