package io.github.java_lan_multiplayer.server.controller.listeners;

import io.github.java_lan_multiplayer.common.messages.PlayersInfoMessage;
import io.github.java_lan_multiplayer.server.controller.MessageDispatcher;
import io.github.java_lan_multiplayer.server.events.lobby.PlayersUpdateEvent;
import io.github.java_lan_multiplayer.server.model.GameModel;

public class LobbyListener extends EventHandler {

    public LobbyListener(GameModel gameModel, MessageDispatcher dispatcher) {
        super(gameModel, dispatcher);
    }


    @Handles(PlayersUpdateEvent.class)
    public void handlePlayersUpdate(PlayersUpdateEvent event) {
        dispatcher.broadcast(new PlayersInfoMessage(event.getPlayerInfoList(), event.getUsername()));
    }
}
