package io.github.java_lan_multiplayer.client;

import io.github.java_lan_multiplayer.client.controllers.lobby.LobbyController;
import io.github.java_lan_multiplayer.common.messages.login.PlayerInfo;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class to manage the current client session state during a game.
 * Stores data such as the current username, profile picture, player information,
 * and session status flags.
 */
public class ClientSession {
    private static String username = null;
    private static int[] profilePicture = null;
    private static Map<String, PlayerInfo> players = new HashMap<>();
    private static boolean givingUp = false;

    public static String getUsername() {
        return username;
    }
    public static void setUsername(String name) {
        username = name;
    }

    public static int[] getProfilePicture() {
        return profilePicture;
    }
    public static void setProfilePicture(int[] profilePicture) {
        ClientSession.profilePicture = profilePicture;
    }

    public static PlayerInfo getPlayerInfo(String username) {
        return players.get(username);
    }
    public static PlayerInfo getPlayerInfo() {
        return players.get(username);
    }
    /**
     * Updates the internal player list with a new set of {@link PlayerInfo} objects.
     * Replaces all existing entries.
     *
     * @param playersInfo the list of updated player information
     */
    public static void updatePlayers(List<PlayerInfo> playersInfo) {
        players = playersInfo.stream().collect(Collectors.toMap(PlayerInfo::getUsername, p -> p));
    }
    /**
     * Checks if the given username belongs to the current player.
     *
     * @param username the username to check
     * @return true if the username matches the current player, false otherwise
     */
    public static boolean isSelf(String username) {
        return username.equals(ClientSession.getUsername());
    }

    public static boolean isGivingUp() {
        return givingUp;
    }

    public static void setGivingUp(boolean givingUp) {
        ClientSession.givingUp = givingUp;
    }

    public static Set<String> getPlayerNames() {
        return players.keySet();
    }

    public static int getPlayerCount() {
        return players.size();
    }

    /**
     * Builds and returns a circular profile picture for the given player.
     * The picture is composed of a background, character body, and accessory,
     * clipped to a circular shape.
     *
     * @param username the username of the player
     * @param size the desired size (width and height) of the profile picture
     * @return a {@link StackPane} containing the composed profile picture,
     *         or null if the player does not exist
     */
    public static StackPane getPlayerProfilePicture(String username, double size) {
        PlayerInfo playerInfo = players.get(username);
        if(playerInfo == null) return null;

        ImageView profilePicBody = new ImageView();
        ImageView profilePicAcc = new ImageView();
        Image bodyImage = new Image(Objects.requireNonNull(LobbyController.class.getResourceAsStream("/textures/characters/character_" + playerInfo.getProfilePicIds()[0] + ".png")));
        Image accessoryImage = new Image(Objects.requireNonNull(LobbyController.class.getResourceAsStream("/textures/characters/hand_" + playerInfo.getProfilePicIds()[1] + ".png")));

        profilePicBody.setImage(bodyImage);
        profilePicBody.setFitHeight(size);
        profilePicBody.setFitWidth(size);

        profilePicAcc.setImage(accessoryImage);
        profilePicAcc.setFitHeight(size);
        profilePicAcc.setFitWidth(size);

        double radius = size / 2;
        Circle profilePicBackground = new Circle(radius);

        profilePicBackground.setFill(Paint.valueOf(playerInfo.getColor()));
        profilePicBackground.setStroke(Color.BLACK);
        profilePicBackground.setStrokeWidth(1);

        // Add a circle clip to the first avatar image to make it circular
        Circle bodyClip = new Circle(radius);
        bodyClip.setCenterX(profilePicBody.getFitWidth() / 2);
        bodyClip.setCenterY(profilePicBody.getFitHeight() / 2);
        profilePicBody.setClip(bodyClip);

        Circle accClip = new Circle(radius);
        accClip.setCenterX(profilePicAcc.getFitWidth() / 2);
        accClip.setCenterY(profilePicAcc.getFitHeight() / 2);
        profilePicAcc.setClip(accClip);

        StackPane profilePic = new StackPane(profilePicBackground, profilePicBody, profilePicAcc);
        profilePic.setMinSize(size, size);
        profilePic.setPrefSize(size, size);
        profilePic.setMaxSize(size, size);
        return profilePic;
    }
}
