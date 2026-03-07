package io.github.java_lan_multiplayer.client.popups;

import io.github.java_lan_multiplayer.client.LanguageManager;
import io.github.java_lan_multiplayer.common.Logger;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class CountdownPopupController implements PopupAware {

    private Popup popup;
    private static boolean isPopupVisible = false;

    @FXML private Label countdownLabel;
    @FXML private Label label;

    private int secondsLeft = 10;

    @Override
    public void bindPopup(Popup popup) {
        this.popup = popup;
    }

    @FXML
    public void onClose() {}

    @Override
    public void initialize(Object... args) {
        if (args == null || args.length == 0) {
            Logger.logWarning("No arguments provided");
            return;
        }

        if (args[0] instanceof String promptText) {
            label.setText(LanguageManager.get(promptText.trim()));
        }

        if(args.length > 1 && args[1] instanceof Integer duration) {
            secondsLeft = duration;
        }
        startCountdown();
    }

    public void startCountdown() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, _ -> updateCountdown()),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(secondsLeft);
        timeline.setOnFinished(_ -> popup.playClosingAnimation());
        timeline.play();
    }

    private void updateCountdown() {
        countdownLabel.setText(String.valueOf(secondsLeft));

        ScaleTransition st = new ScaleTransition(Duration.millis(150), countdownLabel);
        st.setFromX(1.3);
        st.setFromY(1.3);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();

        secondsLeft--;
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
