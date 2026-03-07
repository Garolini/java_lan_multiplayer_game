package io.github.java_lan_multiplayer.client.popups.cards;

import io.github.java_lan_multiplayer.client.ClientSession;
import io.github.java_lan_multiplayer.client.VirtualServer;
import io.github.java_lan_multiplayer.client.popups.Popup;
import io.github.java_lan_multiplayer.client.popups.PopupAware;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.flight.cardActions.PlanetChosenMessage;
import io.github.java_lan_multiplayer.common.messages.flight.cardActions.PlanetRefusedMessage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.List;

public class PlanetsPopupController implements PopupAware {

    private Popup popup;
    private static boolean isPopupVisible = false;

    VirtualServer virtualServer = VirtualServer.getInstance();

    @FXML private ImageView cardImageView;
    @FXML private Button planetButton0;
    @FXML private Button planetButton1;
    @FXML private Button planetButton2;
    @FXML private Button planetButton3;

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
        if(args.length > 1 && args[1] instanceof List<?> planetChosenBy) {
            List<Button> buttons = List.of(planetButton0, planetButton1, planetButton2, planetButton3);

            for(int i = 0; i < planetChosenBy.size(); i++) {
                Button button = buttons.get(i);
                button.setVisible(true);
                if(planetChosenBy.get(i) == null) {
                    button.setDisable(false);
                    final int chosenPlanet = i;
                    button.setOnAction(_ -> {
                        virtualServer.sendMessage(new PlanetChosenMessage(chosenPlanet));
                        popup.playClosingAnimation();
                    });
                } else {
                    button.setStyle("-fx-opacity: 1; -fx-background-color: rgba(200, 200, 200, 0.4); -fx-border-color: gray");
                    StackPane profilePicture = ClientSession.getPlayerProfilePicture((String)planetChosenBy.get(i), 40);
                    if(profilePicture != null) {
                        profilePicture.setTranslateX(-100);
                        button.setGraphic(profilePicture);
                    }
                }
            }
            if(planetChosenBy.size() == 2) {
                planetButton1.setTranslateY(9);
            }
        }
    }

    @FXML
    public void onDecline() {
        virtualServer.sendMessage(new PlanetRefusedMessage());
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
