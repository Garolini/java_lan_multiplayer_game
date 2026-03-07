package io.github.java_lan_multiplayer.client.popups;

import io.github.java_lan_multiplayer.client.ClientSession;
import io.github.java_lan_multiplayer.client.LanguageManager;
import io.github.java_lan_multiplayer.client.VirtualServer;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.SimpleMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SettingsController implements PopupAware {

    private Popup popup;
    private static boolean isPopupVisible = false;

    @FXML private Button languageButton;
    @FXML private Label languageNoteLabel;

    @FXML private Button debugButton;
    @FXML private Button mainMenuButton;

    @FXML private VBox giveUpBox;
    @FXML private Button giveUpButton;
    @FXML private Label giveUpNoteLabel;

    VirtualServer virtualServer = VirtualServer.getInstance();

    @Override
    public void bindPopup(Popup popup) {
        this.popup = popup;
    }

    @FXML
    public void initialize() {
        updateTexts();
    }

    @FXML
    public void onLanguageChange() {
        LanguageManager.cycleLanguages();
        updateTexts();
    }

    @FXML
    public void onDebugModeChange() {
        Logger.setDebugMode(!Logger.isDebugEnabled());
        updateTexts();
    }

    @FXML
    public void onMainMenuPressed() {
        Popup.newConfirmationPopup("prompt.main_menu", () -> {
            Logger.logInfo("Returning to main menu...");
            VirtualServer.getInstance().close();
        }).show();
    }

    @FXML
    public void onCloseGamePressed() {
        Popup.newConfirmationPopup("prompt.close_game", () -> {
            Logger.logInfo("Closing game...");
            VirtualServer.getInstance().close();
            Platform.exit();
            System.exit(0);
        }).show();
    }

    private void onGivUpPressed() {
        if(ClientSession.isGivingUp()) return;
        virtualServer.sendMessage(new SimpleMessage("give_up"));
        ClientSession.setGivingUp(true);
        updateTexts();
    }

    @FXML
    public void onClose() {
        popup.playClosingAnimation();
    }

    @Override
    public void initialize(Object... args) {
        if(args == null || args.length == 0) return;

        if(args[0] instanceof Boolean isMainMenu) {
            mainMenuButton.setDisable(isMainMenu);
        }

        if(args.length > 1 && args[1] instanceof Boolean isFlight) {
            if(isFlight) {
                giveUpButton.setOnAction(_ -> onGivUpPressed());
                giveUpBox.setVisible(true);
            }
        }

        updateTexts();
    }

    private void updateTexts() {

        String langKey = LanguageManager.getPendingLocale().toLanguageTag().toLowerCase();
        boolean isCurrent = LanguageManager.getLocale().equals(LanguageManager.getPendingLocale());
        languageButton.setText(LanguageManager.get("button.language_" + langKey));
        languageButton.setStyle("-fx-font-size: 14; -fx-background-color: " + (isCurrent ? "gold" : "orange"));
        languageNoteLabel.setVisible(!isCurrent);

        boolean isDebug = Logger.isDebugEnabled();
        debugButton.setText(LanguageManager.get(isDebug? "button.on" : "button.off"));
        debugButton.setStyle("-fx-font-size: 14; -fx-background-color: " + (isDebug ? "gold" : "orange"));

        boolean givingUp = ClientSession.isGivingUp();
        giveUpButton.setDisable(givingUp);
        giveUpNoteLabel.setVisible(givingUp);
    }

    @Override
    public void setPopupVisible(boolean visible) {
        isPopupVisible = visible;
    }

    @Override
    public boolean isPopupVisible() {
        return isPopupVisible;
    }
}
