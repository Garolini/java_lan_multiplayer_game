package io.github.java_lan_multiplayer.server.events.lobby;

import io.github.java_lan_multiplayer.common.messages.login.PlayerInfo;
import io.github.java_lan_multiplayer.server.events.EventModel;

import java.util.List;

public class PlayersUpdateEvent extends EventModel {

    public final List<PlayerInfo> playerInfoList;
    public final String username;

    public PlayersUpdateEvent(List<PlayerInfo> playerInfoList, String username) {
        this.playerInfoList = playerInfoList;
        this.username = username;
    }

    public List<PlayerInfo> getPlayerInfoList() {
        return playerInfoList;
    }

    public String getUsername() {
        return username;
    }
}
