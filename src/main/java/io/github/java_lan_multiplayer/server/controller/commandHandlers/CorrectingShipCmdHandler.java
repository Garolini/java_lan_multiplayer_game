package io.github.java_lan_multiplayer.server.controller.commandHandlers;

import io.github.java_lan_multiplayer.common.messages.flight.SelectedTileMessage;
import io.github.java_lan_multiplayer.server.controller.MessageDispatcher;
import io.github.java_lan_multiplayer.server.model.GameModel;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.VirtualView;

import java.util.Map;

/**
 * Handles commands related to correcting ship placement during the setup phase of the game.
 * <p>
 * This handler allows players to select and remove a chosen tile on their board.
 */
public class CorrectingShipCmdHandler extends CommandHandler {

    /**
     * Constructs a {@code CorrectingShipCmdHandler} with the provided game model, player mapping, and dispatcher.
     *
     * @param gameModel  the current game model
     * @param playerMap  a mapping of virtual views to their corresponding players
     * @param dispatcher the message dispatcher used for sending and receiving messages
     */
    public CorrectingShipCmdHandler(GameModel gameModel, Map<VirtualView, Player> playerMap, MessageDispatcher dispatcher) {
        super(gameModel, playerMap, dispatcher);
    }

    /**
     * Registers the command handler for tile selection by players.
     */
    @Override
    protected void registerHandlers() {
        register("selected_tile", (view, cmd) -> handleSelectedTile(view, mapper.convertValue(cmd, SelectedTileMessage.class)));
    }


    private void handleSelectedTile(VirtualView senderView, SelectedTileMessage message) {
        Player player = playerMap.get(senderView);

        gameModel.removeSelectedTile(player, message.getX(), message.getY());
    }
}
