package io.github.java_lan_multiplayer.client.popups;

import io.github.java_lan_multiplayer.client.VirtualServer;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.shipBuilding.CardsReturnedMessage;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;

public class CardsPopupController implements PopupAware {

    private Popup popup;
    private static boolean isPopupVisible = false;

    @FXML private HBox cardBox;
    @FXML private Button closeButton;

    private boolean isClosing = false;

    @Override
    public void bindPopup(Popup popup) {
        this.popup = popup;
    }

    @FXML
    public void onClose() {
        if(isClosing) return;
        isClosing = true;

        if (popup != null) {
            VirtualServer.getInstance().sendMessage(new CardsReturnedMessage());
            playClosingCardAnimation();
        }
    }

    @Override
    public void initialize(Object... args) {
        if(args == null || args.length == 0) {
            Logger.logWarning("No arguments provided");
            return;
        }

        if (args[0] instanceof int[] levelOneCards) {
            for (int levelOneCard : levelOneCards) {
                ImageView card = new ImageView(loadCardImage(1, levelOneCard));
                card.setPreserveRatio(true);
                card.setFitHeight(400);
                addShadowEffect(card);
                cardBox.getChildren().add(card);
            }
        }
        if (args.length > 1 && args[1] instanceof int[] levelTwoCards) {
            for (int levelTwoCard : levelTwoCards) {
                ImageView card = new ImageView(loadCardImage(2, levelTwoCard));
                card.setPreserveRatio(true);
                card.setFitHeight(400);
                addShadowEffect(card);
                cardBox.getChildren().add(card);
            }
        }
        if(!cardBox.getChildren().isEmpty()) Platform.runLater(this::playOpeningCardAnimation);
    }

    private Image loadCardImage(int level, int id) {
        return new Image(Objects.requireNonNull(getClass().getResourceAsStream("/textures/cards/card_level_" + level + "_" + id + ".png")));
    }

    private void playOpeningCardAnimation() {
        closeButton.setDisable(true);
        double boxWidth = cardBox.getWidth();
        double centerX = boxWidth / 2;

        List<Node> cards = cardBox.getChildren();
        ParallelTransition parallelTransition = new ParallelTransition();

        for (Node node : cards) {
            if (node instanceof ImageView imageView) {
                double imageViewCenterX = imageView.getLayoutX() + imageView.getBoundsInParent().getWidth() / 2;
                double fromTranslateX = centerX - imageViewCenterX;

                imageView.setTranslateX(fromTranslateX);

                TranslateTransition transition = new TranslateTransition(Duration.millis(300), imageView);
                transition.setToX(0);
                parallelTransition.getChildren().add(transition);
            }
        }

        parallelTransition.setOnFinished(_ -> closeButton.setDisable(false));
        parallelTransition.play();
    }

    private void playClosingCardAnimation() {
        double boxWidth = cardBox.getWidth();
        double centerX = boxWidth / 2;

        List<Node> cards = cardBox.getChildren();
        ParallelTransition parallelTransition = new ParallelTransition();

        for (Node node : cards) {
            if (node instanceof ImageView imageView) {
                double imageViewCenterX = imageView.getLayoutX() + imageView.getBoundsInParent().getWidth() / 2;
                double translateX = centerX - imageViewCenterX;

                TranslateTransition transition = new TranslateTransition(Duration.millis(200), imageView);
                transition.setByX(translateX);
                parallelTransition.getChildren().add(transition);
            }
        }

        parallelTransition.setOnFinished(_ -> popup.playClosingAnimation());
        parallelTransition.play();
    }

    private void addShadowEffect(ImageView card) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(new Color(.2, .2, .2, .8));
        shadow.setWidth(100);
        shadow.setHeight(100);
        shadow.setRadius(20);

        card.setEffect(shadow);
    }

    @Override
    public void setPopupVisible(boolean visible) {
        isPopupVisible = visible;
    }

    public static void setPopupVisible() {
        isPopupVisible = false;
    }

    @Override
    public boolean isPopupVisible() {
        return isPopupVisible;
    }
}
