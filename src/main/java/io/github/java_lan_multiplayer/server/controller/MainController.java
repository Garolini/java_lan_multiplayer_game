package io.github.java_lan_multiplayer.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.java_lan_multiplayer.common.messages.BoardTypeMessage;
import io.github.java_lan_multiplayer.common.messages.login.JoinRequestMessage;
import io.github.java_lan_multiplayer.common.messages.login.PlayerInfo;
import io.github.java_lan_multiplayer.server.controller.listeners.*;
import io.github.java_lan_multiplayer.server.model.GameModel;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.common.messages.login.JoinResponseMessage;
import io.github.java_lan_multiplayer.server.VirtualView;

import java.util.*;

/**
 * Main controller class that coordinates between the virtual views (clients), the game model,
 * and the backend logic through command routing and event dispatching.
 *
 * <p>This class manages:
 * <ul>
 *   <li>Routing incoming JSON commands from clients</li>
 *   <li>Maintaining the mapping between VirtualViews and Players</li>
 *   <li>Initializing listeners for various gameplay events</li>
 *   <li>Handling player joining and disconnection logic</li>
 * </ul>
 */
public class MainController {

    private final GameModel gameModel;
    private final Map<VirtualView, Player> playerMap = new HashMap<>();

    private final MessageDispatcher dispatcher = new MessageDispatcher(playerMap);
    private final CommandRouter router;

    /**
     * Constructs a new MainController instance, initializing the game model and setting up event listeners.
     *
     * @param gameModel the shared game model for managing game state
     */
    public MainController(GameModel gameModel) {
        this.gameModel = gameModel;
        this.router = new CommandRouter(gameModel, playerMap, dispatcher);

        initializeListeners();
    }

    /**
     * Registers various event listeners related to different game subsystems,
     * such as lobby, building, game flow, ships, and cards.
     */
    private void initializeListeners() {
        List<EventHandler> handlers = List.of(
                new LobbyListener(gameModel, dispatcher),
                new BuildingListener(gameModel, dispatcher),
                new GameListener(gameModel, dispatcher),
                new ShipListener(gameModel, dispatcher),
                new CardsListener(gameModel, dispatcher)
        );
        handlers.forEach(handler -> EventScanner.registerHandlers(gameModel, handler, dispatcher));
    }

    /**
     * Processes a command received from a client (VirtualView), decoding the incoming JSON string.
     * Delegates the command either to player info handling or command routing.
     *
     * @param senderVirtualView the client sending the command
     * @param json the JSON string representing the command
     * @throws JsonProcessingException if the JSON parsing fails
     */
    public void processCommand(VirtualView senderVirtualView, String json) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode cmd = mapper.readTree(json);
        String type = cmd.get("type").asText();

        if(type.equals("join_request")) {
            handleJoinRequest(senderVirtualView, mapper.convertValue(cmd, JoinRequestMessage.class));
        }
        else {
            router.route(gameModel.getGameState(), senderVirtualView, cmd);
        }
    }

    /**
     * Handles the initial message from a client containing player information (e.g., username, avatar).
     * Validates and registers the player to the game if allowed.
     *
     * @param senderVirtualView the client sending the info
     * @param message        the player's basic info
     */
    private void handleJoinRequest(VirtualView senderVirtualView, JoinRequestMessage message) {
        PlayerInfo playerInfo = message.getPlayerInfo();
        Player player = new Player(playerInfo.getUsername(), playerInfo.getProfilePicIds());

        JoinResponseMessage.JoinStatus result = gameModel.canAddPlayer(player);

        senderVirtualView.sendMessage(new JoinResponseMessage(result));

        if (result == JoinResponseMessage.JoinStatus.SUCCESS) {
            player.assignEventDispatcher(gameModel.getEventDispatcher());
            senderVirtualView.sendMessage(new BoardTypeMessage(gameModel.getBoardType()));
            playerMap.put(senderVirtualView, player);
            gameModel.addPlayer(player);
        } else senderVirtualView.close();
    }

    /**
     * Removes a client from the game, cleaning up the player mapping and notifying the game model.
     *
     * @param virtualView the disconnected client
     */
    public void removeClient(VirtualView virtualView) {
        synchronized (playerMap) {
            if(playerMap.containsKey(virtualView)) {
                String playerName = playerMap.get(virtualView).getName();
                playerMap.remove(virtualView);
                this.gameModel.removePlayer(playerName);
            }
        }
    }
}
