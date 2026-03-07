package io.github.java_lan_multiplayer.client.popups;

import io.github.java_lan_multiplayer.client.LanguageManager;
import io.github.java_lan_multiplayer.common.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ConfirmationPopupController implements PopupAware {

    private Popup popup;
    private static boolean isPopupVisible = false;

    @FXML private Label prompt;

    private Runnable onConfirmAction = null;

    @Override
    public void bindPopup(Popup popup) {
        this.popup = popup;
    }

    @FXML
    public void onClose() {
        if (popup != null) popup.playClosingAnimation();
    }

    @FXML
    public void onConfirm() {
        if (popup != null) popup.playClosingAnimation();
        if (onConfirmAction != null) onConfirmAction.run();
    }

    @Override
    public void initialize(Object... args) {

        if (args == null || args.length == 0) {
            Logger.logWarning("No arguments provided");
            return;
        }

        if (args[0] instanceof String promptText) {
            prompt.setText(LanguageManager.get(promptText.trim()));
        }

        if (args.length > 1 && args[1] instanceof Runnable action) {
            this.onConfirmAction = action;
        }
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
