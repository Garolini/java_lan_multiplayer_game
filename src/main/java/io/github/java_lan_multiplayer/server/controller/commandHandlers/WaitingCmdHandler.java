package io.github.java_lan_multiplayer.server.controller.commandHandlers;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.VirtualView;
import io.github.java_lan_multiplayer.server.controller.MessageDispatcher;
import io.github.java_lan_multiplayer.server.model.GameModel;
import io.github.java_lan_multiplayer.server.model.Player;

import java.util.Map;

/**
 * Command handler used while the server is in a waiting state.
 * <p>
 * This handler does not register any specific command logic and ignores all incoming commands.
 * It logs a warning for each received command without taking further action.
 */
public class WaitingCmdHandler extends CommandHandler {

    public WaitingCmdHandler(GameModel gameModel, Map<VirtualView, Player> playerMap, MessageDispatcher dispatcher) {
        super(gameModel, playerMap, dispatcher);
    }

    /**
     * Registers command handlers.
     * <p>
     * This implementation intentionally does nothing since no commands should be handled
     * in the waiting state.
     */
    @Override
    protected void registerHandlers() {}

    /**
     * Handles incoming commands during the waiting state.
     * <p>
     * This implementation logs a warning and ignores the command.
     *
     * @param senderView the virtual view that sent the command
     * @param cmd        the received command as a JSON node
     */
    @Override
    public void handle(VirtualView senderView, JsonNode cmd) {
        Logger.logWarning("Server is currently waiting, ignoring command: " + cmd);
    }
}
