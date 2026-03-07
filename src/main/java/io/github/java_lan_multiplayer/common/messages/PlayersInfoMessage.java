package io.github.java_lan_multiplayer.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.login.PlayerInfo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayersInfoMessage implements GameMessage {

    private List<PlayerInfo> players;
    private String username;


    public PlayersInfoMessage() {}

    public PlayersInfoMessage(List<PlayerInfo> players, String username) {
        this.players = players;
        this.username = username;
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getType() {
        return "players_info";
    }
}
