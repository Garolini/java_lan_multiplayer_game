package io.github.java_lan_multiplayer.common.messages.endGame;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayersScoresMessage implements GameMessage {

    private List<PlayerScores> playersScores;

    public PlayersScoresMessage() {}

    public PlayersScoresMessage(List<PlayerScores> playersScores) {
        this.playersScores = playersScores;
    }

    public List<PlayerScores> getPlayersScores() {
        return playersScores;
    }

    @Override
    public String getType() {
        return "players_scores";
    }
}
