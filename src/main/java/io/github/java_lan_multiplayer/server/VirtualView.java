package io.github.java_lan_multiplayer.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.GameMessage;
import io.github.java_lan_multiplayer.common.messages.SimpleMessage;
import io.github.java_lan_multiplayer.server.controller.MainController;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Handles communication between the server and a single connected client.
 * This class manages input/output streams, message parsing, heartbeat pings,
 * and forwards messages to the main game controller.
 *
 * <p>Each client is handled in its own thread.</p>
 */
public class VirtualView implements Runnable {
    private final BufferedReader in;
    private final PrintWriter out;
    private final Socket clientSocket;
    private final MainController controller;
    private Timer pingTimer;

    /**
     * Constructs a VirtualView with the given socket and controller.
     * Initializes input and output streams.
     *
     * @param clientSocket the socket connected to the client
     * @param controller the controller handling game logic
     * @throws RuntimeException if the I/O streams fail to initialize
     */
    public VirtualView(Socket clientSocket, MainController controller) {
        this.clientSocket = clientSocket;
        this.controller = controller;

        try{
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        new Thread(this).start();
    }

    /**
     * Sends a message object to the client after converting it to JSON.
     * Logs a warning if the message is not of expected type {@code GameMessage}.
     *
     * @param message the message to send
     */
    public void sendMessage(GameMessage message) {

        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        this.out.println(json);
    }

    /**
     * Starts listening for incoming messages from the client.
     * Forwards them to the {@link MainController} for processing.
     * Sends heartbeat pings periodically.
     */
    @Override
    public void run() {
        Logger.logInfo("Client connected: " + clientSocket.getRemoteSocketAddress());
        startPingLoop();

        try {
            while (true) {
                String msg = in.readLine();
                Logger.logDebug("RECEIVED: " + msg);
                if (msg == null) {
                    Logger.logInfo("Client disconnected.");
                    break;
                }
                this.controller.processCommand(this, msg);
            }

        } catch (SocketException e) {
            Logger.logWarning("Client disconnected: " + e.getMessage());
        } catch (IOException e) {
            if ("Stream closed".equals(e.getMessage())) {
                Logger.logInfo("Stream closed locally.");
            } else {
                Logger.logError("IOException: " + e.getMessage());
            }
        } finally {
            close();
        }
    }

    private void startPingLoop() {
        pingTimer = new Timer(true);
        pingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendMessage(new SimpleMessage("ping"));
            }
        }, 3000, 3000);  // send every 3s
    }

    private volatile boolean closed = false;

    /**
     * Closes all resources associated with this client and notifies the controller.
     * This method is safe to call multiple times.
     */
    public void close() {
        if (closed) return;
        closed = true;

        this.controller.removeClient(this);

        try {

            if (in != null) in.close();
            if (out != null) out.close();

            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                Logger.logDebug("Client connection closed");
            }
            if (pingTimer != null) pingTimer.cancel();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
