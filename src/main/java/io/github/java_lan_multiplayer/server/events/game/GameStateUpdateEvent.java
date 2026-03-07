package io.github.java_lan_multiplayer.server.events.game;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.GameModel;

public class GameStateUpdateEvent extends EventModel {
    private final GameModel.GameState previousGameState;
    private final GameModel.GameState gameState;

    public GameStateUpdateEvent(GameModel.GameState previousGameState, GameModel.GameState gameState) {
        this.previousGameState = previousGameState;
        this.gameState = gameState;
    }

    public GameModel.GameState getPreviousGameState() {
        return previousGameState;
    }

    public GameModel.GameState getGameState() {
        return gameState;
    }
}
