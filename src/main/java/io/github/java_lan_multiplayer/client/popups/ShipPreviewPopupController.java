package io.github.java_lan_multiplayer.client.popups;

import io.github.java_lan_multiplayer.client.ClientSession;
import io.github.java_lan_multiplayer.client.LanguageManager;
import io.github.java_lan_multiplayer.client.controllers.flight.ShipHandler;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.flight.TileInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.*;

public class ShipPreviewPopupController implements PopupAware {

    private Popup popup;
    private static boolean isPopupVisible = false;

    @FXML private HBox titleBox;
    @FXML private ImageView boardView;
    @FXML private GridPane tilesGrid;

    String playerColor = "blue";

    private boolean isClosing = false;

    @Override
    public void bindPopup(Popup popup) {
        this.popup = popup;
    }

    @FXML
    public void onClose() {
        if(isClosing) return;
        isClosing = true;

        if (popup != null) popup.playClosingAnimation();
    }

    @Override
    public void initialize(Object... args) {
        if(args == null || args.length == 0) {
            Logger.logWarning("No arguments provided");
            return;
        }

        if(args[0] instanceof String playerName) {
            StackPane profilePicture = ClientSession.getPlayerProfilePicture(playerName, 50);
            Label title = new Label(LanguageManager.get("label.player_ship", playerName));
            title.setStyle("-fx-font-size: 20; -fx-font-weight: bold");
            titleBox.getChildren().addAll(profilePicture, title);
            playerColor = ClientSession.getPlayerInfo(playerName).getColor();
        }

        if(args.length > 1 && args[1] instanceof Image boardImage) {
            boardView.setImage(boardImage);
        }

        if (args.length > 2 && args[2] instanceof Set<?> set ) {
            Set<TileInfo> tiles = new HashSet<>();
            for (Object obj : set) {
                if (obj instanceof TileInfo ps) {
                    tiles.add(ps);
                } else {
                    Logger.logWarning("Set contains invalid element: " + obj);
                }
            }
            renderShip(tiles);
        }
    }

    private void renderShip(Set<TileInfo> tiles) {
        for(TileInfo tileInfo : tiles) {
            ImageView tile = new ImageView(loadTileImage(tileInfo.getId(), playerColor));
            tile.setPreserveRatio(true);
            tile.setFitHeight(71);
            tile.setFitWidth(71);
            tile.setRotate(tileInfo.getRotation() * 90);

            tilesGrid.add(tile, tileInfo.getX(), tileInfo.getY());
        }
    }

    private Image loadTileImage(int id, String color) {
        String path = (id < 0 || id > 151)
                ? "/textures/tiles/tile_starter_cabin_" + color.toLowerCase() + ".png"
                : "/textures/tiles/tile_" + id + ".png";
        return new Image(Objects.requireNonNull(ShipHandler.class.getResourceAsStream(path)));
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
