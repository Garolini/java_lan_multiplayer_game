package io.github.java_lan_multiplayer.client;

import io.github.java_lan_multiplayer.client.popups.Popup;
import io.github.java_lan_multiplayer.common.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.Objects;

/**
 * Main entry point for the Java Lan Multiplayer Game client application.
 * <p>
 * This class initializes the JavaFX UI, sets up fonts, icons, scenes,
 * and handles command-line arguments such as language and debug mode.
 */
public class ClientMain extends Application {

    /**
     * Called by the JavaFX runtime to start the application.
     * Sets up fonts, main stage properties, and loads the login scene.
     *
     * @param stage the primary stage for this application
     */
    @Override
    public void start(Stage stage) {

        Font.loadFont(getClass().getResourceAsStream("/fonts/DynaPuff-Regular.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/fonts/DynaPuff-Bold.ttf"), 12);

        SceneSwitch.setStage(stage);

        stage.setTitle("Java Lan Multiplayer Game");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/textures/icons/mainIcon.png"))));
        stage.setResizable(false);

        SceneSwitch.switchScene("LoginScene");

        stage.setOnCloseRequest(event -> {
            event.consume();
            Popup.newConfirmationPopup("prompt.close_game", () -> {
                Logger.logInfo("Closing game...");
                VirtualServer.getInstance().close();
                Platform.exit();
                System.exit(0);
            }).show();
        });

        stage.show();
    }

    /**
     * Main method that serves as the application entry point.
     * <p>
     * Supported command-line arguments:
     * <ul>
     *   <li><b>-l, --lang [en|it]</b>: Sets the UI language. Defaults to English if unsupported.</li>
     *   <li><b>-d, --debug</b>: Enables debug logging mode.</li>
     * </ul>
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        Logger.logInfo("Starting Java Lan Multiplayer Game...");

        CommandLineOptions options = parseCommandLine(args);
        // Apply selected settings
        LanguageManager.setPendingLocale(options.selectedLocale());
        Logger.setDebugMode(options.debugMode());

        launch();
    }

    private record CommandLineOptions(Locale selectedLocale, boolean debugMode) {}

    private static CommandLineOptions parseCommandLine(String[] args) {
        Locale selectedLocale = Locale.ENGLISH;
        boolean debugMode = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-l", "--lang" -> {
                    if (i + 1 < args.length) {
                        String langCode = args[++i].toLowerCase();
                        switch (langCode) {
                            case "en":
                                selectedLocale = Locale.ENGLISH;
                                break;
                            case "it":
                                selectedLocale = Locale.ITALIAN;
                                break;
                            default:
                                Logger.logWarning("Unsupported language: " + langCode + ". Defaulting to English.");
                        }
                    } else {
                        Logger.logWarning("Missing language code after --lang. Defaulting to English.");
                    }
                }

                case "-d", "--debug" -> debugMode = true;

                default -> Logger.logWarning("Unknown argument: " + args[i]);
            }
        }
        return new CommandLineOptions(selectedLocale, debugMode);
    }
}
