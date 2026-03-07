package io.github.java_lan_multiplayer.client;

import io.github.java_lan_multiplayer.client.popups.Popup;
import io.github.java_lan_multiplayer.common.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.net.URL;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Utility class for managing JavaFX scene transitions in the application.
 * Maintains a reference to the primary stage and handles loading FXML files,
 * applying localization, and initializing controllers.
 */
public class SceneSwitch {
    private static Stage primaryStage;
    private static String currentFxml = null;


    /**
     * Sets the primary stage used for scene switching.
     *
     * @param stage the main JavaFX stage
     */
    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Switches the current scene to the one specified by the FXML filename.
     * Prevents reloading if the requested scene is already active.
     *
     * <p>This method performs the following steps:
     * <ul>
     *   <li>Closes any open popups</li>
     *   <li>Loads the specified FXML file from the <code>/fxml/scenes/</code> directory</li>
     *   <li>Applies the pending locale</li>
     *   <li>Initializes the scene and sets it on the primary stage</li>
     *   <li>Invokes {@code updateCallBack()} on controllers implementing {@link VirtualServerAware}</li>
     * </ul>
     *
     * @param fxmlFile the name of the FXML file (without the ".fxml" extension)
     */
    public static void switchScene(String fxmlFile) {
        if (fxmlFile.equals(currentFxml)) return;

        Popup.closeAllPopups();
        URL resource = SceneSwitch.class.getResource("/fxml/scenes/" + fxmlFile + ".fxml");

        Parent root;
        try {
            if (resource == null) throw new IOException("FXML file not found: " + fxmlFile + ".fxml");

            LanguageManager.applyPendingLocale();
            ResourceBundle bundle = LanguageManager.getBundle();

            FXMLLoader fxmlLoader = new FXMLLoader(resource, bundle);
            root = fxmlLoader.load();

            Object controller = fxmlLoader.getController();
            if (controller instanceof VirtualServerAware aware) {
                aware.updateCallBack();
            }
        } catch (IOException e) {
            Logger.logError("Error while switching scenes: " + e.getMessage());
            return;
        }

        Scene newScene = new Scene(root);
        primaryStage.setScene(newScene);
        currentFxml = fxmlFile;

        if (!primaryStage.isShowing()) primaryStage.show();
    }

    /**
     * Returns the primary stage currently used by the application.
     *
     * @return the JavaFX primary stage
     */
    public static Stage getStage() {
        return primaryStage;
    }
}