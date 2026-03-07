package io.github.java_lan_multiplayer.client.popups.cards;

import io.github.java_lan_multiplayer.client.VirtualServer;
import io.github.java_lan_multiplayer.client.popups.Popup;
import io.github.java_lan_multiplayer.client.popups.PopupAware;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.flight.cardActions.BatteryRefuseMessage;
import io.github.java_lan_multiplayer.common.messages.flight.cardActions.CargoDoneMessage;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ShipPopupController implements PopupAware {

    private static boolean isPopupVisible = false;

    VirtualServer virtualServer = VirtualServer.getInstance();

    @FXML private StackPane shipPane;
    @FXML private StackPane blockPoolPane;

    @Override
    public void bindPopup(Popup popup) {}

    @FXML
    public void onClose() {}

    @Override
    public void initialize(Object... args) {

        if (args == null || args.length == 0) {
            Logger.logWarning("No arguments provided");
            return;
        }

        if (args[0] instanceof StackPane incomingShip) {
            shipPane.getChildren().add(incomingShip);
        }

        if(args.length > 1 && args[1] instanceof VBox blockPool) {
            this.blockPoolPane.getChildren().add(blockPool);
        }
    }

    @FXML
    public void onBatteryDecline(Event event) {
        ((Node)event.getTarget()).setDisable(true);
        virtualServer.sendMessage(new BatteryRefuseMessage());
    }

    @FXML
    public void onCargoDone(Event event) {
        ((Node)event.getTarget()).setDisable(true);
        virtualServer.sendMessage(new CargoDoneMessage());
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
