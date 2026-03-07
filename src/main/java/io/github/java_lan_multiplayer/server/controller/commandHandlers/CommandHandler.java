package io.github.java_lan_multiplayer.server.controller.commandHandlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.controller.MessageDispatcher;
import io.github.java_lan_multiplayer.server.model.GameModel;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.VirtualView;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Abstract base class for handling JSON-based commands in the game architecture.
 * <p>
 * Provides the foundation for registering and executing command handlers associated
 * with string-based command types. Subclasses are responsible for defining the command types
 * they handle by implementing {@link #registerHandlers()}.
 */
public abstract class CommandHandler {
    protected final GameModel gameModel;
    protected final Map<VirtualView, Player> playerMap;
    protected final MessageDispatcher dispatcher;

    private final Map<String, BiConsumer<VirtualView, JsonNode>> commandHandlers = new HashMap<>();
    protected final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructs a new {@code CommandHandler} with the specified game model, player map, and dispatcher.
     * Automatically invokes {@link #registerHandlers()} to populate supported command types.
     *
     * @param gameModel  the current game model
     * @param playerMap  a mapping between virtual views and players
     * @param dispatcher the dispatcher responsible for communication between components
     */
    public CommandHandler(GameModel gameModel, Map<VirtualView, Player> playerMap, MessageDispatcher dispatcher) {
        this.gameModel = gameModel;
        this.playerMap = playerMap;
        this.dispatcher = dispatcher;
        registerHandlers();
    }

    /**
     * Registers all command types this handler supports.
     * <p>
     * Subclasses must implement this method to associate command types with their logic.
     */
    protected abstract void registerHandlers();

    /**
     * Registers a handler function for a specific command type.
     *
     * @param type    the string identifier of the command (e.g., "move", "ready_toggle")
     * @param handler the logic to execute when the command is received
     */
    protected void register(String type, BiConsumer<VirtualView, JsonNode> handler) {
        commandHandlers.put(type, handler);
    }

    /**
     * Handles an incoming command by dispatching it to the appropriate handler based on its type.
     * <p>
     * If no handler is registered for the command type, {@link #handleUnrecognizedCommand(VirtualView, JsonNode)} is called.
     *
     * @param senderView the virtual view representing the sender
     * @param cmd        the JSON command object
     */
    public void handle(VirtualView senderView, JsonNode cmd) {
        String type = cmd.get("type").asText();
        BiConsumer<VirtualView, JsonNode> handler = commandHandlers.get(type);

        if (handler != null) {
            handler.accept(senderView, cmd);
        } else {
            handleUnrecognizedCommand(senderView, cmd);
        }
    }

    /**
     * Handles a command that does not match any registered handler.
     * <p>
     * This base implementation logs a warning. Subclasses may override for custom behavior.
     *
     * @param view the view that sent the unrecognized command
     * @param cmd  the unrecognized command
     */
    protected void handleUnrecognizedCommand(VirtualView view, JsonNode cmd) {
        Logger.logWarning("Unrecognized command: " + cmd.get("type").asText());
    }
}