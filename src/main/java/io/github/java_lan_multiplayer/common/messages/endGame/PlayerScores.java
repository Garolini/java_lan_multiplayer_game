package io.github.java_lan_multiplayer.common.messages.endGame;

public class PlayerScores {

    private String playerName;

    private int positionScore;
    private int bestShipScore;
    private int cargoScore;
    private int lostTilesScore;
    private int finalScore;

    public PlayerScores() {}

    public PlayerScores(String playerName, int positionScore, int bestShipScore, int cargoScore, int lostTilesScore, int finalScore) {
        this.playerName = playerName;
        this.positionScore = positionScore;
        this.bestShipScore = bestShipScore;
        this.cargoScore = cargoScore;
        this.lostTilesScore = lostTilesScore;
        this.finalScore = finalScore;
    }

    public String getPlayerName() {
        return playerName;
    }
    public int getPositionScore() {
        return positionScore;
    }
    public int getBestShipScore() {
        return bestShipScore;
    }
    public int getCargoScore() {
        return cargoScore;
    }
    public int getLostTilesScore() {
        return lostTilesScore;
    }
    public int getFinalScore() {
        return finalScore;
    }
}