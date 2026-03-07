package io.github.java_lan_multiplayer.client.popups.cards;

import io.github.java_lan_multiplayer.client.VirtualServer;
import io.github.java_lan_multiplayer.client.popups.Popup;
import io.github.java_lan_multiplayer.client.popups.PopupAware;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.flight.cardActions.CardDecisionMessage;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DecisionPopupController implements PopupAware {

    private Popup popup;
    private static boolean isPopupVisible = false;

    VirtualServer virtualServer = VirtualServer.getInstance();

    @FXML private ImageView cardImageView;

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

        if (args[0] instanceof Image cardImage) {
            cardImageView.setImage(cardImage);
        }
    }

    @FXML
    public void onAccept() {
        virtualServer.sendMessage(new CardDecisionMessage(null, true));
        popup.playClosingAnimation();
    }

    @FXML
    public void onDecline() {
        virtualServer.sendMessage(new CardDecisionMessage(null, false));
        popup.playClosingAnimation();
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
