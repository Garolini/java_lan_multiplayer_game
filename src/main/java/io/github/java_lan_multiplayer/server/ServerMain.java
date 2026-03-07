package io.github.java_lan_multiplayer.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.java_lan_multiplayer.server.model.GameModel;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.controller.MainController;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Enumeration;

/**
 * The entry point for the server application.
 * <p>Parses command-line arguments, loads configuration, initializes the game controller,
 * starts a server socket, and accepts client connections via {@link VirtualView}.</p>
 */
public class ServerMain {
    private static final MainController controller = new MainController(new GameModel());

    private record CommandLineOptions(Integer portNumber, boolean debugMode) {}

    public static void main(String[] args) {

        // Parse command-line arguments
        CommandLineOptions options = parseCommandLine(args);
        Integer portNumber = options.portNumber;

        // Load from config if no valid port provided
        if (portNumber == null) {
            portNumber = loadPortFromJson(portNumber);
        }

        Logger.logInfo("Initializing server...");
        Logger.setDebugMode(options.debugMode());

        try {
            Logger.logInfo("Server running on IP: " + getLocalAddress()
                    + " (LocalHostAddress: " + InetAddress.getLocalHost().getHostAddress() + ")");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // Start server socket
        ServerSocket serverSocket = initializeServerSocket(portNumber, options);
        if (serverSocket == null) return;

        // Main server loop
        handleIncomingConnections(serverSocket);
    }

    private static ServerSocket initializeServerSocket(Integer portNumber, CommandLineOptions options) {
        ServerSocket serverSocket;
        try {
            serverSocket = (portNumber == null) ? new ServerSocket(0) : new ServerSocket(portNumber);
        } catch (IOException e) {
            Logger.logError("Could not listen on port: " + options.portNumber());
            System.exit(1);
            return null;
        }

        Logger.logInfo("Listening on port " + serverSocket.getLocalPort());
        return serverSocket;
    }

    private static CommandLineOptions parseCommandLine(String[] args) {

        Integer portNumber = null;
        boolean debugMode = false;
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-p", "--port" -> {
                    if (i + 1 < args.length) {
                        try {
                            portNumber = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            Logger.logWarning("Invalid port number: " + args[i] + ". Falling back to config file.");
                        }
                    } else {
                        Logger.logWarning("Missing port number after --port. Falling back to config file.");
                    }
                }

                case "-d", "--debug" -> debugMode = true;

                default -> Logger.logWarning("Unknown argument: " + args[i]);
            }
        }

        return new CommandLineOptions(portNumber, debugMode);
    }

    private static Integer loadPortFromJson(Integer portNumber) {
        try (InputStream config = ServerMain.class.getClassLoader().getResourceAsStream("public/config.json")) {
            if (config == null) throw new IOException("Could not find config.json");
            portNumber = new ObjectMapper().readTree(config).get("port").asInt();
        } catch (IOException e) {
            Logger.logError("Could not read config.json: " + e.getMessage());
        }
        return portNumber;
    }

    private static void handleIncomingConnections(ServerSocket serverSocket) {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                Logger.logDebug("New client accepted");
                VirtualView virtualView = new VirtualView(clientSocket, controller);
                virtualView.start();
            } catch (IOException e) {
                Logger.logError("Accept failed: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    private static String getLocalAddress() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while(nets.hasMoreElements()) {
                NetworkInterface netInt = nets.nextElement();
                String name = netInt.getDisplayName().toLowerCase();
                if(netInt.isUp() && !netInt.isLoopback() && !name.contains("virtual") && !name.contains("hyper-v")) {
                    Enumeration<InetAddress> addresses = netInt.getInetAddresses();
                    while(addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        String ip = addr.getHostAddress();
                        if(!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                            return ip;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Logger.logError("Error fetching network interfaces: " + e.getMessage());
        }
        return "Unknown Address";
    }
}
