package io.github.java_lan_multiplayer.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.java_lan_multiplayer.client.popups.PendingPopup;
import io.github.java_lan_multiplayer.client.popups.Popup;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.GameMessage;
import javafx.application.Platform;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Handles the client-side network connection to a remote server.
 *
 * <p>This singleton class manages socket communication, background threads for message listening and
 * connection health monitoring (via ping), and delegates received messages to the UI layer via a callback interface.</p>
 */
public class VirtualServer {
    private Socket clientSocket;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private long lastPingTime;

    private UICallBack UICallBack;
    private final Queue<String> messageQueue = new LinkedList<>();

    private static VirtualServer instance;

    private VirtualServer() {}

    /**
     * Returns the singleton instance of the {@code VirtualServer}.
     *
     * @return the {@code VirtualServer} instance
     */
    public static VirtualServer getInstance() {
        if(instance == null){
            instance = new VirtualServer();
        }
        return instance;
    }

    /**
     * Attempts to establish a socket connection to the specified server.
     * Starts a daemon thread to listen for incoming messages.
     *
     * @param hostName   the server hostname or IP address
     * @param portNumber the port to connect to
     * @throws IOException if the connection fails
     */
    public void attemptConnection(String hostName, int portNumber) throws IOException {
        clientSocket = new Socket();
        clientSocket.connect(new InetSocketAddress(hostName, portNumber), 500);
        disconnecting = false;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            Logger.logError("Error setting up streams: " + e.getMessage());
            return;
        }

        Thread readerThread = new Thread(this::listenForMessages);
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private volatile boolean disconnecting = false;

    /**
     * Starts listening for messages from the server in a blocking loop.
     * Automatically handles disconnections, pings, and UI message dispatch.
     */
    public void listenForMessages() {
        Logger.logInfo("ClientThread started");

        lastPingTime = System.currentTimeMillis();
        Thread watchdog = getWatchdog();
        watchdog.start();

        try {
            while (true) {
                String msg = in.readLine();
                Logger.logDebug("RECEIVED MESSAGE: " + msg);

                if (msg == null) {
                    Logger.logInfo("Client disconnected normally.");
                    PendingPopup.set(Popup.newAlertPopup("title.disconnected", "description.server_closed"));
                    break;
                }

                handleMessageFromServer(msg);
            }
        } catch (SocketException e) {
            if (disconnecting) {
                Logger.logInfo("Client disconnected itself normally.");
            } else {
                Logger.logWarning("Client forcibly disconnected.");
                PendingPopup.set(Popup.newAlertPopup("title.disconnected", "description.connection_lost"));
            }
        } catch (IOException e) {
            Logger.logError("IOException: " + e.getMessage());
            //PendingPopup.set(Popup.newAlertPopup("title.disconnected", "description.unexpected_error"));
        } finally {
            close();
            Platform.runLater(() -> SceneSwitch.switchScene("LoginScene"));
        }
    }

    private Thread getWatchdog() {
        Thread watchdog = new Thread(() -> {
            try {
                while (!disconnecting) {
                    Thread.sleep(2000);
                    if (System.currentTimeMillis() - lastPingTime > 4000) {
                        Logger.logWarning("No ping received, assuming disconnection.");
                        PendingPopup.set(Popup.newAlertPopup("title.disconnected", "description.connection_lost"));
                        close();
                        break;
                    }
                }
            } catch (InterruptedException ignored) {}
        });
        watchdog.setDaemon(true);
        return watchdog;
    }

    /**
     * Closes the socket connection and associated input/output streams.
     * Marks the client as disconnecting to prevent watchdog from reopening alerts.
     */
    public void close() {
        disconnecting = true;
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                Logger.logDebug("Client connection closed.");
            }
            if (in != null) in.close();
            if (out != null) out.close();

        } catch (IOException e) {
            Logger.logError("Error closing server connection: " + e.getMessage());
        }
    }

    /**
     * Sends a message to the server by serializing it into JSON.
     *
     * @param message the message to send
     * @throws RuntimeException if serialization fails
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
     * Handles an incoming JSON-formatted message from the server.
     * Automatically updates ping timestamp or delegates to the UI.
     *
     * @param msg the message received from the server
     */
    public void handleMessageFromServer(String msg) {
        try {
            String type = new ObjectMapper().readTree(msg).get("type").asText();
            if (type.equals("ping")) {
                lastPingTime = System.currentTimeMillis();
                return;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        synchronized(this) {
            if (UICallBack != null) {
                dispatchToUI(UICallBack, msg);
            } else {
                messageQueue.add(msg);
            }
        }
    }

    /**
     * Sets the callback used to deliver messages to the UI.
     * All queued messages are flushed to the new callback if provided.
     *
     * @param newCallBack the callback implementation to use
     */
    public synchronized void setCallBack(UICallBack newCallBack) {
        this.UICallBack = newCallBack;
        if (newCallBack != null) {
            while (!messageQueue.isEmpty()) {
                String queued = messageQueue.poll();
                dispatchToUI(newCallBack, queued);
            }
        }
    }

    /**
     * Dispatches a message to the UI through the provided callback.
     *
     * @param callBack the UI callback to use
     * @param msg      the message to dispatch
     * @throws RuntimeException if JSON parsing fails
     */
    private void dispatchToUI(UICallBack callBack, String msg) {
        try {
            callBack.process(msg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
