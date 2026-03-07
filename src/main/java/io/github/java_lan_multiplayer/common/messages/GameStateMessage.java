package io.github.java_lan_multiplayer.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.server.model.GameModel;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameStateMessage implements GameMessage {

    private GameModel.GameState gameState;

    public GameStateMessage() {}

    public GameStateMessage(GameModel.GameState gameState) {
       this.gameState = gameState;
    }

    public GameModel.GameState getGameState() {
       return gameState;
    }

    @Override
    public String getType() {
        return "game_state";
    }
}
