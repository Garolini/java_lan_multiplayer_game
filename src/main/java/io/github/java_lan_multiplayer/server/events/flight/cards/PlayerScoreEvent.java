package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class PlayerScoreEvent extends EventModel {

    private final String playerName;
    private final String scoreType;
    private final double score;

    public PlayerScoreEvent(String playerName, String scoreType, double score) {
        this.playerName = playerName;
        this.scoreType = scoreType;
        this.score = score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getScoreType() {
        return scoreType;
    }

    public double getScore() {
        return score;
    }
}
