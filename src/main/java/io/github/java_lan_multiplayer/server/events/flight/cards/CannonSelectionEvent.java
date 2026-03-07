package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.Player;

public class CannonSelectionEvent extends EventModel {

    private final Player player;

    public CannonSelectionEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
