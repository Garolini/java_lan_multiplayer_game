package io.github.java_lan_multiplayer.client.popups.cards;

import io.github.java_lan_multiplayer.client.LanguageManager;
import io.github.java_lan_multiplayer.client.VirtualServer;
import io.github.java_lan_multiplayer.client.popups.Popup;
import io.github.java_lan_multiplayer.client.popups.PopupAware;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.flight.cardActions.ActivateCannonsMessage;
import io.github.java_lan_multiplayer.common.messages.flight.cardActions.ActivateEnginesMessage;
import io.github.java_lan_multiplayer.server.model.ShipBoard;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TileSelectionPopupController implements PopupAware {

    private static boolean isPopupVisible = false;

    VirtualServer virtualServer = VirtualServer.getInstance();

    @FXML private StackPane shipPane;
    @FXML private GridPane tilesGrid;
    @FXML private Label titleLabel;
    @FXML private Label mismatchNoteLabel;
    @FXML private Button doneButton;

    @FXML private ImageView typeView;
    @FXML private Label powerCountLabel;

    private String tileType;
    private double minPower;
    private final Set<Point> activeVertical = new HashSet<>();
    private final Set<Point> activeRotated = new HashSet<>();
    private final Map<Point, Integer> usedBatteries = new HashMap<>();
    private boolean isOpenSpace = false;
    private boolean hasAlien = false;

    private final Image selectableImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/textures/sprites/overlays/selectable.png")));
    private final Image selectedImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/textures/sprites/overlays/selected.png")));
    private final Image battery_1 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/textures/sprites/overlays/battery_1.png")));
    private final Image battery_2 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/textures/sprites/overlays/battery_2.png")));
    private final Image battery_3 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/textures/sprites/overlays/battery_3.png")));

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
        if (args.length > 1 && args[1] instanceof String type) {
            this.tileType = type.toLowerCase();
            configureLabelsForTileType();
        }
        if (args.length > 2 && args[2] instanceof Double power) {
            minPower = power;
            powerCountLabel.setText((minPower % 1 == 0) ? String.format("%.0f", minPower) : String.format("%.1f", minPower));
        }
        Set<ShipBoard.BatteriesData> batteries = extractSetFromArgs(args, 3, ShipBoard.BatteriesData.class);
        Set<Point> verticalTiles = extractSetFromArgs(args, 4, Point.class);
        Set<Point> rotatedTiles = extractSetFromArgs(args, 5, Point.class);

        if (args.length > 6 && args[6] instanceof Boolean openSpace) {
            this.isOpenSpace = openSpace;
        }
        if (args.length > 7 && args[7] instanceof Boolean alien) {
            this.hasAlien = alien;
        }

        setupBoard(verticalTiles, rotatedTiles, batteries);
    }

    @FXML
    public void onDone() {
        double currentPower = minPower + 2 * activeVertical.size() + activeRotated.size();

        if(isOpenSpace && currentPower == 0) {
            Popup.newConfirmationPopup("prompt.no_engine_power", this::sendData).show();
        } else {
            sendData();
        }
    }

    public void sendData() {
        doneButton.setDisable(true);
        int verticalCount = activeVertical.size();
        int rotatedCount = activeRotated.size();
        List<Point> batteries = new ArrayList<>();

        for (Map.Entry<Point, Integer> entry : usedBatteries.entrySet()) {
            Point point = entry.getKey();
            int count = entry.getValue();

            for (int i = 0; i < count; i++) {
                batteries.add(point);
            }
        }
        switch (tileType) {
            case "cannons" -> virtualServer.sendMessage(new ActivateCannonsMessage(verticalCount, rotatedCount, batteries));
            case "engines" -> virtualServer.sendMessage(new ActivateEnginesMessage(verticalCount, batteries));
        }
    }

    private void configureLabelsForTileType() {
        switch (tileType) {
            case "cannons" -> {
                titleLabel.setText(LanguageManager.get("label.select_cannons"));
                mismatchNoteLabel.setText(LanguageManager.get("label.cannons_mismatch"));
                typeView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/textures/sprites/cannon.png"))));
            }
            case "engines" -> {
                titleLabel.setText(LanguageManager.get("label.select_engines"));
                mismatchNoteLabel.setText(LanguageManager.get("label.engines_mismatch"));
                typeView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/textures/sprites/engine.png"))));
            }
            default -> Logger.logWarning("Unknown tile type: " + tileType);
        }
    }

    private void setupBoard(Set<Point> verticalTiles, Set<Point> rotatedTiles, Set<ShipBoard.BatteriesData> batteries) {

        for (Point point : verticalTiles) {
            ImageView img = getOverlayView();
            img.setOnMouseClicked(_ -> onVerticalClicked(img, point));
            tilesGrid.add(img, point.x, point.y);
        }

        for (Point point : rotatedTiles) {
            ImageView img = getOverlayView();
            img.setOnMouseClicked(_ -> onRotatedClicked(img, point));
            tilesGrid.add(img, point.x, point.y);
        }

        for (ShipBoard.BatteriesData data : batteries) {
            if(data.batteries() == 0) continue;
            ImageView img = getOverlayView();
            img.setOnMouseClicked(_ -> onBatteryClicked(img, new Point(data.x(), data.y()), data.batteries()));
            tilesGrid.add(img, data.x(), data.y());
        }
    }

    private ImageView getOverlayView() {
        ImageView img = new ImageView(selectableImage);
        img.setPickOnBounds(true);
        img.setCursor(Cursor.HAND);
        img.setFitHeight(71);
        img.setFitWidth(71);
        return img;
    }

    private void onVerticalClicked(ImageView overlay, Point point) {
        if(activeVertical.contains(point)) {
            activeVertical.remove(point);
            overlay.setImage(selectableImage);
        } else {
            activeVertical.add(point);
            overlay.setImage(selectedImage);
        }
        updateStatus();
    }
    private void onRotatedClicked(ImageView overlay, Point point) {
        if(activeRotated.contains(point)) {
            activeRotated.remove(point);
            overlay.setImage(selectableImage);
        } else {
            activeRotated.add(point);
            overlay.setImage(selectedImage);
        }
        updateStatus();
    }
    private void onBatteryClicked(ImageView overlay, Point point, int maxBatteries) {
        int current = usedBatteries.getOrDefault(point, 0);
        int next = current + 1;

        if (next > maxBatteries) {
            usedBatteries.remove(point);
            overlay.setImage(selectableImage);
        } else {
            usedBatteries.put(point, next);

            switch (next) {
                case 1 -> overlay.setImage(battery_1);
                case 2 -> overlay.setImage(battery_2);
                case 3 -> overlay.setImage(battery_3);
            }
        }
        updateStatus();
    }

    private void updateStatus() {
        boolean isSelectionBalanced = isSelectionBalanced();

        doneButton.setDisable(!isSelectionBalanced);
        mismatchNoteLabel.setVisible(!isSelectionBalanced);

        double currentPower = minPower + 2 * activeVertical.size() + activeRotated.size();
        if(minPower == 0 && hasAlien && currentPower > 0) currentPower += 2;
        powerCountLabel.setText((currentPower % 1 == 0) ? String.format("%.0f", currentPower) : String.format("%.1f", currentPower));
    }

    private boolean isSelectionBalanced() {
        int activeTileCount = activeVertical.size() + activeRotated.size();
        int batteryCount = usedBatteries.values().stream().mapToInt(Integer::intValue).sum();
        return activeTileCount == batteryCount;
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> extractSetFromArgs(Object[] args, int index, Class<T> expectedType) {
        Set<T> result = new HashSet<>();
        if (args.length > index && args[index] instanceof Set<?> set) {
            for (Object obj : set) {
                if (expectedType.isInstance(obj)) {
                    result.add((T) obj);
                } else {
                    Logger.logWarning("Set at index " + index + " contains invalid element: " + obj);
                }
            }
        }
        return result;
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
