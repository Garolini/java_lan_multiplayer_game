package io.github.java_lan_multiplayer.client.popups.cards;

import io.github.java_lan_multiplayer.client.ClientSession;
import io.github.java_lan_multiplayer.client.LanguageManager;
import io.github.java_lan_multiplayer.client.popups.Popup;
import io.github.java_lan_multiplayer.client.popups.PopupAware;
import io.github.java_lan_multiplayer.common.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Objects;
import java.util.Random;

public class DiceThrowPopupController implements PopupAware {

    private Popup popup;
    private static boolean isPopupVisible = false;

    private static final Image[] DICE_IMAGES = new Image[6];

    @FXML private Label titleLabel, sumLabel;
    @FXML private StackPane profilePicturePane;
    @FXML private ImageView die1View, die2View;
    private int finalDie1, finalDie2;

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

        if (args[0] instanceof String playerName) {
            StackPane profilePicture = ClientSession.getPlayerProfilePicture(playerName, 80);
            profilePicturePane.getChildren().add(profilePicture);
            titleLabel.setText(LanguageManager.get("label.rolling_dice", playerName));
        }

        if (args.length > 2 && args[1] instanceof Integer die1 && args[2] instanceof Integer die2) {
            this.finalDie1 = die1;
            this.finalDie2 = die2;
            playAnimation();
        }
    }

    private void playAnimation() {
        Timeline diceRoll = new Timeline();
        Random rng = new Random();

        for (int i = 0; i < 20; i++) {
            int delay = i * 100;
            diceRoll.getKeyFrames().add(new KeyFrame(Duration.millis(delay), _ -> die1View.setImage(getDieImage(rng.nextInt(6) + 1))));
        }
        diceRoll.getKeyFrames().add(new KeyFrame(Duration.millis(2000), _ -> die1View.setImage(getDieImage(finalDie1))));

        for (int i = 0; i < 30; i++) {
            int delay = i * 100;
            diceRoll.getKeyFrames().add(new KeyFrame(Duration.millis(delay), _ -> die2View.setImage(getDieImage(rng.nextInt(6) + 1))));
        }
        diceRoll.getKeyFrames().add(new KeyFrame(Duration.millis(3000), _ -> {
            die1View.setImage(getDieImage(finalDie1));
            die2View.setImage(getDieImage(finalDie2));
            sumLabel.setText(String.valueOf(finalDie1 + finalDie2));

            PauseTransition pause = new PauseTransition(Duration.millis(1200));
            pause.setOnFinished(_ -> popup.playClosingAnimation());
            pause.play();
        }));
        diceRoll.play();
    }

    static {
        for (int i = 0; i < 6; i++) {
            DICE_IMAGES[i] = new Image(Objects.requireNonNull(DiceThrowPopupController.class.getResourceAsStream("/textures/sprites/dice/die_" + (i + 1) + ".png")));

        }
    }

    private static Image getDieImage(int value) {
        return DICE_IMAGES[value - 1];
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
