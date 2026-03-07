package io.github.java_lan_multiplayer.server.controller;

import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.GameMessage;
import io.github.java_lan_multiplayer.server.VirtualView;
import io.github.java_lan_multiplayer.server.model.Player;

import java.util.Map;

/**
 * The {@code MessageDispatcher} is responsible for sending messages
 * between the server and connected clients via their associated {@link VirtualView}.
 *
 * <p>It uses the provided {@link Map} linking each {@link VirtualView} to its {@link Player}
 * to determine where to send messages. It supports broadcasting messages to all players,
 * to all players except one, or to a specific player by name.</p>
 */
public class MessageDispatcher {
    private final Map<VirtualView, Player> playerMap;

    /**
     * Constructs a {@code MessageDispatcher} using the provided player map.
     *
     * @param playerMap a map associating each {@link VirtualView} with a {@link Player}
     */
    public MessageDispatcher(Map<VirtualView, Player> playerMap) {
        this.playerMap = playerMap;
    }

    /**
     * Sends a message to all connected players.
     *
     * @param message the message object to be sent
     */
    public void broadcast(GameMessage message) {
        synchronized (playerMap) {
            for (VirtualView virtualView : playerMap.keySet()) {
                virtualView.sendMessage(message);
            }
        }
    }

    /**
     * Sends a message to all players except the one with the specified name.
     *
     * @param playerName the name of the player to exclude
     * @param message    the message object to be sent
     */
    public void broadcastExcept(String playerName, GameMessage message) {
        synchronized (playerMap) {
            for (VirtualView virtualView : playerMap.keySet()) {
                if(playerMap.get(virtualView).getName().equals(playerName)) continue;
                virtualView.sendMessage(message);
            }
        }
    }

    /**
     * Sends a message to a specific player by name.
     *
     * @param playerName the name of the target player
     * @param message    the message object to be sent
     */
    public void sendTo(String playerName, GameMessage message) {
        VirtualView virtualView = getVirtualViewByPlayerName(playerName);
        virtualView.sendMessage(message);
    }

    /**
     * Retrieves the {@link VirtualView} associated with the given player's username.
     *
     * @param username the name of the player
     * @return the corresponding {@link VirtualView}, or {@code null} if not found
     */
    public VirtualView getVirtualViewByPlayerName(String username) {
        for (Map.Entry<VirtualView, Player> entry : playerMap.entrySet()) {
            if (entry.getValue().getName().equals(username)) {
                return entry.getKey();
            }
        }
        Logger.logError("No VirtualView found for player: " + username);
        return null;
    }
}