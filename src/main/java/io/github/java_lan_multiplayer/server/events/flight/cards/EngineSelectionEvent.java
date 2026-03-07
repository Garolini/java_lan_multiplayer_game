package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.Player;

public class EngineSelectionEvent extends EventModel {

    private final Player player;
    private final boolean isOpenSpace;

    public EngineSelectionEvent(Player player, boolean isOpenSpace) {
        this.player = player;
        this.isOpenSpace = isOpenSpace;
    }

    public Player getPlayer() {
        return player;
    }
    public boolean isOpenSpace() {
        return isOpenSpace;
    }
}
