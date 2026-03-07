package io.github.java_lan_multiplayer.server.events.gameOver;

import io.github.java_lan_multiplayer.common.messages.endGame.PlayerScores;
import io.github.java_lan_multiplayer.server.events.EventModel;

import java.util.List;

public class GameOverEvent extends EventModel {

    private final List<PlayerScores> playersScores;

    public GameOverEvent(List<PlayerScores> playersScores) {
        this.playersScores = playersScores;
    }

    public List<PlayerScores> getPlayersScores() {
        return playersScores;
    }
}

