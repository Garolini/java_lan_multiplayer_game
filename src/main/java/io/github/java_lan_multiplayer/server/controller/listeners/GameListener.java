package io.github.java_lan_multiplayer.server.controller.listeners;

import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.BoardTypeMessage;
import io.github.java_lan_multiplayer.common.messages.GameResetMessage;
import io.github.java_lan_multiplayer.common.messages.GameStateMessage;
import io.github.java_lan_multiplayer.common.messages.PlayersInfoMessage;
import io.github.java_lan_multiplayer.common.messages.endGame.PlayersScoresMessage;
import io.github.java_lan_multiplayer.common.messages.flight.DeckLevelsMessage;
import io.github.java_lan_multiplayer.common.messages.flight.PickAvailableMessage;
import io.github.java_lan_multiplayer.common.messages.flight.PlayerPositionMessage;
import io.github.java_lan_multiplayer.common.messages.flight.ShipBoardMessage;
import io.github.java_lan_multiplayer.server.controller.MessageDispatcher;
import io.github.java_lan_multiplayer.server.events.game.GameResetEvent;
import io.github.java_lan_multiplayer.server.events.game.GameStateUpdateEvent;
import io.github.java_lan_multiplayer.server.events.gameOver.GameOverEvent;
import io.github.java_lan_multiplayer.server.events.lobby.BoardTypeUpdateEvent;
import io.github.java_lan_multiplayer.server.model.GameModel;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.Card;

import java.util.List;

public class GameListener extends EventHandler {

    public GameListener(GameModel gameModel, MessageDispatcher dispatcher) {
        super(gameModel, dispatcher);
    }


    @Handles(GameResetEvent.class)
    private void handleGameReset(GameResetEvent event) {
        Logger.logDebug("Game resetting...");
        dispatcher.broadcast(new GameResetMessage(event.getPlayerName()));
        dispatcher.broadcast(new BoardTypeMessage(gameModel.getBoardType()));
    }

    @Handles(BoardTypeUpdateEvent.class)
    private void handleBoardTypeUpdate(BoardTypeUpdateEvent event) {
        Logger.logDebug("Updated board type: " + event.getBoardType());
        dispatcher.broadcast(new BoardTypeMessage(event.getBoardType()));
    }

    @Handles(GameStateUpdateEvent.class)
    private void handleGameStateUpdate(GameStateUpdateEvent event) {
        Logger.logInfo("New Game State: " + event.getGameState().toString().toLowerCase().replace("_", " "));
        dispatcher.broadcast(new GameStateMessage(event.getGameState()));
        sendNewGameStateData(event.getPreviousGameState(), event.getGameState());
    }

    @Handles(GameOverEvent.class)
    private void handleGameOver(GameOverEvent event) {
        dispatcher.broadcast(new PlayersScoresMessage(event.getPlayersScores()));
        dispatcher.broadcast(new PlayersInfoMessage(gameModel.getPlayersInfo(), null));
        dispatcher.broadcast(new BoardTypeMessage(gameModel.getBoardType()));
    }


    private void sendNewGameStateData(GameModel.GameState previousGameState, GameModel.GameState gameState) {
        switch(gameState) {
            case WAITING -> {
                if(previousGameState == GameModel.GameState.LOBBY) {
                    dispatcher.broadcast(new BoardTypeMessage(gameModel.getBoardType()));
                    dispatcher.broadcast(new PlayersInfoMessage(gameModel.getPlayersInfo(), null));
                }}
            case CORRECTING_SHIP -> {
                dispatcher.broadcast(new BoardTypeMessage(gameModel.getBoardType()));
                for(Player player : gameModel.getSortedPlayers()) {
                    String playerName = player.getName();
                    dispatcher.broadcast(new ShipBoardMessage(playerName, player.getSimplifiedShipBoard()));
                    dispatcher.broadcast(new PlayerPositionMessage(playerName, player.getPosition()));
                }
                gameModel.shuffleDeck();
                List<Integer> cardsLevel = gameModel.getDeck().stream().map(Card::getLevel).toList();
                dispatcher.broadcast(new DeckLevelsMessage(cardsLevel));
            }
            case PICKING_CARDS -> dispatcher.broadcast(new PickAvailableMessage(gameModel.getFirstPlayer().getName()));
        }
    }
}
