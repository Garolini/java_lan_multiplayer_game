package io.github.java_lan_multiplayer.client.popups;

import io.github.java_lan_multiplayer.client.LanguageManager;
import io.github.java_lan_multiplayer.client.SceneSwitch;
import io.github.java_lan_multiplayer.common.messages.endGame.PlayerScores;
import io.github.java_lan_multiplayer.common.messages.flight.TileInfo;
import io.github.java_lan_multiplayer.server.model.ShipBoard;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Utility class for managing modal popup windows in a JavaFX application.
 * <p>
 * Popups are dynamically loaded from FXML files and support optional arguments for controller initialization.
 * They automatically apply visual effects (e.g., blur) to background nodes and support animated opening and closing.
 * <p>
 * This class supports a wide range of specialized popups via static factory methods, each preconfigured with the appropriate FXML file and constructor arguments.
 */
public class Popup {

    private static final List<PopupAware> activePopups = new ArrayList<>();

    private final Node content;
    private final Region overlay;

    private final Map<Node, Effect> originalEffects = new HashMap<>();

    private final Object controller;
    private boolean isClosing = false;

    private Popup(String fxmlFile, Object... args) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popups/" + fxmlFile + ".fxml"), LanguageManager.getBundle());
            Node content = loader.load();

            controller = loader.getController();
            if (controller instanceof PopupAware controllerAware) {
                controllerAware.bindPopup(this);
                controllerAware.initialize(args);
                activePopups.add(controllerAware);
            }

            this.content = content;

            this.overlay = new Region();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2);");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlFile, e);
        }
    }

    /**
     * Displays the popup window with animations and background blur.
     * Automatically binds ESC key to close the popup.
     *
     * @throws IllegalStateException if the root of the current scene is not a {@link StackPane}
     */
    public void show() {
        if (controller instanceof PopupAware controllerAware) {
            if(controllerAware.isPopupVisible()) return;
            controllerAware.setPopupVisible(true);
        }
        Scene scene = SceneSwitch.getStage().getScene();
        if (!(scene.getRoot() instanceof StackPane rootPane)) {
            throw new IllegalStateException("Popup requires scene root to be a StackPane");
        }

        ParallelTransition openingAnimation = getOpeningAnimation();

        rootPane.getChildren().addAll(overlay, content);
        openingAnimation.play();

        applyBlur(rootPane);

        overlay.setPrefSize(rootPane.getWidth(), rootPane.getHeight());
        overlay.setOnMouseClicked(_ -> close());

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });
    }

    private void close() {
        if(isClosing) return;
        isClosing = true;

        if (controller instanceof PopupAware controllerAware) {
            controllerAware.onClose();
        }
        else playClosingAnimation();
    }

    /**
     * Triggers the closing animation and removes the popup from the scene.
     * Ensures the controller is notified if it implements {@link PopupAware}.
     */
    public void playClosingAnimation() {
        StackPane rootPane = getRootPane();
        if(rootPane == null) return;

        clearBlur();
        content.setMouseTransparent(true);

        ParallelTransition closingAnimation = getClosingAnimation();

        closingAnimation.setOnFinished(_ -> {
            rootPane.getChildren().removeAll(overlay, content);
            if (controller instanceof PopupAware controllerAware) {
                controllerAware.setPopupVisible(false);
                activePopups.remove(controllerAware);
            }
        });

        closingAnimation.play();
    }

    /**
     * Creates a new Settings popup.
     *
     * @param isMainMenu whether the popup is opened from the main menu
     * @param isFlight whether the popup is opened during a flight
     * @return a new instance of {@code Popup}
     */
    public static Popup newSettingsPopup(boolean isMainMenu, boolean isFlight) {
        return new Popup("SettingsPopup", isMainMenu, isFlight);
    }
    /**
     * Creates a new Settings popup with just the main menu context.
     *
     * @param isMainMenu whether the popup is opened from the main menu
     * @return a new instance of {@code Popup}
     */
    public static Popup newSettingsPopup(boolean isMainMenu) {
        return new Popup("SettingsPopup", isMainMenu);
    }
    /**
     * Creates a new Settings popup with default context.
     *
     * @return a new instance of {@code Popup}
     */
    public static Popup newSettingsPopup() {
        return new Popup("SettingsPopup");
    }

    /**
     * Creates an alert popup with a title, description, and optional arguments.
     *
     * @param title the title of the alert
     * @param description the main message
     * @param descriptionArgs extra arguments for formatting the description
     * @return a new instance of {@code Popup}
     */
    public static Popup newAlertPopup(String title, String description, String descriptionArgs) {
        return new Popup("AlertPopup", title, description, descriptionArgs);
    }
    public static Popup newAlertPopup(String title, String description) {
        return new Popup("AlertPopup", title, description);
    }

    public static Popup newCardsPopup(int[] levelOneCards, int[] levelTwoCards) {
        return new Popup("CardsPopup", levelOneCards, levelTwoCards);
    }

    public static Popup newShipPreviewPopup(String playerName, Image board, Set<TileInfo> tiles) {
        return new Popup("ShipPreviewPopup", playerName, board, tiles);
    }

    public static Popup newCountdownPopup(String text, int duration) {
        return new Popup("CountdownPopup", text, duration);
    }

    public static Popup newConfirmationPopup(String prompt, Runnable action) {
        return new Popup("ConfirmationPopup", prompt, action);
    }

    public static Popup newPlanetsCardPopup(Image cardImage, List<String> planetChosenBy) {
        return new Popup("cards/PlanetsCardPopup", cardImage, planetChosenBy);
    }

    public static Popup newCargoDecisionPopup(StackPane ship, VBox blockPool) {
        return new Popup("cards/CargoDecisionPopup", ship, blockPool);
    }

    public static Popup newCardDecisionPopup(Image cardImage) {
        return new Popup("cards/CardDecisionPopup", cardImage);
    }

    public static Popup newDiceThrowPopup(String playerName, int die1, int die2) {
        return new Popup("cards/DiceThrowPopup", playerName, die1, die2);
    }

    public static Popup newCrewDecisionPopup(StackPane ship) {
        return new Popup("cards/CrewDecisionPopup", ship);
    }

    public static Popup newBatteryDecisionPopup(StackPane ship) {
        return new Popup("cards/BatteryDecisionPopup", ship);
    }

    public static Popup newTileSelectionPopup(StackPane ship, String tileType, double minPower, Set<ShipBoard.BatteriesData> batteries, Set<Point> verticalTiles, Set<Point> rotatedTiles, boolean isOpenSpace, boolean hasAlien) {
        return new Popup("cards/TileSelectionPopup", ship, tileType, minPower, batteries, verticalTiles, rotatedTiles, isOpenSpace, hasAlien);
    }

    public static Popup newFinalScoresPopup(List<PlayerScores> playersScores) {
        return new Popup("FinalScoresPopup", playersScores);
    }

    /**
     * Closes all active popups and resets their visibility.
     * This also clears all visual blur and effect states.
     */
    public static void closeAllPopups() {
        for (PopupAware controller : activePopups) {
            controller.setPopupVisible(false);
        }
        activePopups.clear();
    }


    private StackPane getRootPane() {
        Scene scene = SceneSwitch.getStage().getScene();
        if (!(scene.getRoot() instanceof StackPane rootPane)) {
            throw new IllegalStateException("Popup requires scene root to be a StackPane");
        }
        return rootPane;
    }

    private void applyBlur(StackPane rootPane) {
        for (Node node : rootPane.getChildren()) {
            if (node == content || node == overlay) continue;

            originalEffects.put(node, node.getEffect());

            BoxBlur blur = new BoxBlur(5, 5, 5);
            if (node.getEffect() != null) {
                blur.setInput(node.getEffect());
            }

            node.setEffect(blur);
            node.setMouseTransparent(true);
        }
    }

    private void clearBlur() {
        for (Node node : originalEffects.keySet()) {
            node.setEffect(originalEffects.get(node));
            node.setMouseTransparent(false);
        }
        originalEffects.clear();
    }

    private ParallelTransition getOpeningAnimation() {
        FadeTransition fadeContent = new FadeTransition(Duration.millis(200), content);
        fadeContent.setFromValue(0);
        fadeContent.setToValue(1);
        FadeTransition fadeOverlay = new FadeTransition(Duration.millis(200), overlay);
        fadeOverlay.setFromValue(0);
        fadeOverlay.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(200), content);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);

        return new ParallelTransition(fadeContent, fadeOverlay, scale);
    }
    private ParallelTransition getClosingAnimation() {
        FadeTransition fadeContent = new FadeTransition(Duration.millis(150), content);
        fadeContent.setFromValue(1);
        fadeContent.setToValue(0);
        FadeTransition fadeOverlay = new FadeTransition(Duration.millis(150), overlay);
        fadeOverlay.setFromValue(1);
        fadeOverlay.setToValue(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(150), content);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.8);
        scale.setToY(0.8);

        return new ParallelTransition(fadeContent, fadeOverlay, scale);
    }
}
