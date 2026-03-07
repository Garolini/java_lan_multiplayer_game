package io.github.java_lan_multiplayer.client.controllers.flight;

import io.github.java_lan_multiplayer.client.VirtualServer;
import io.github.java_lan_multiplayer.common.messages.flight.TileInfo;
import io.github.java_lan_multiplayer.common.messages.flight.cardActions.BatteryUseMessage;
import io.github.java_lan_multiplayer.common.messages.flight.cardActions.RemoveMemberMessage;
import io.github.java_lan_multiplayer.server.model.ShipBoard;
import io.github.java_lan_multiplayer.server.model.tiles.CrewType;
import javafx.animation.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.awt.*;
import java.util.*;

public class ShipHandler {

    public static void updateShipPane(GridPane shipGrid, Set<TileInfo> tiles, String color, double cellSize) {

        Map<Point, Node> existingTiles = getCurrentTileMap(shipGrid);
        Set<Point> newTilePositions = placeNewTiles(shipGrid, tiles, color, cellSize, existingTiles);
        animateAndRemoveOldTiles(shipGrid, existingTiles, newTilePositions);
    }

    private static Map<Point, Node> getCurrentTileMap(GridPane shipGrid) {
        Map<Point, Node> map = new HashMap<>();
        for (Node node : shipGrid.getChildren()) {
            Integer x = GridPane.getColumnIndex(node);
            Integer y = GridPane.getRowIndex(node);
            if (x != null && y != null) {
                map.put(new Point(x, y), node);
            }
        }
        return map;
    }

    private static Set<Point> placeNewTiles(GridPane shipGrid, Set<TileInfo> tiles, String color, double cellSize, Map<Point, Node> existingTiles) {
        Set<Point> positions = new HashSet<>();

        for (TileInfo tileInfo : tiles) {
            Point pos = new Point(tileInfo.getX(), tileInfo.getY());
            positions.add(pos);

            if (existingTiles.containsKey(pos)) continue;

            Image image = loadTileImage(tileInfo.getId(), color);
            ImageView tile = createTileImageView(image, tileInfo.getRotation(), cellSize);
            shipGrid.add(tile, tileInfo.getX(), tileInfo.getY());
        }

        return positions;
    }

    private static Image loadTileImage(int id, String color) {
        String path = (id < 0 || id > 151)
                ? "/textures/tiles/tile_starter_cabin_" + color.toLowerCase() + ".png"
                : "/textures/tiles/tile_" + id + ".png";
        return new Image(Objects.requireNonNull(ShipHandler.class.getResourceAsStream(path)));
    }

    private static ImageView createTileImageView(Image image, int rotation, double cellSize) {
        ImageView tile = new ImageView(image);
        tile.setPreserveRatio(true);
        tile.setCache(true);
        tile.setFitWidth(cellSize);
        tile.setFitHeight(cellSize);
        tile.setRotate(rotation * 90);
        return tile;
    }

    private static void animateAndRemoveOldTiles(GridPane shipGrid, Map<Point, Node> existingTiles, Set<Point> newTilePositions) {
        for (Map.Entry<Point, Node> entry : existingTiles.entrySet()) {
            if (!newTilePositions.contains(entry.getKey())) {
                Node node = entry.getValue();

                int col = GridPane.getColumnIndex(node);
                int row = GridPane.getRowIndex(node);

                // Center point of the grid
                double dx = col - 3;
                double dy = row - 2;

                ParallelTransition animation = FlightAnim.getTileRemovalAnimation(dx, dy, node);
                animation.setOnFinished(_ -> shipGrid.getChildren().remove(node));
                animation.play();
            }
        }
    }


    public static void updateCrewPane(GridPane crewGrid, Set<ShipBoard.CrewData> crewData, double cellSize) {
        crewGrid.getChildren().clear();

        for (ShipBoard.CrewData cabinData : crewData) {
            if(cabinData.crew() == CrewType.NONE) continue;
            String imagePath = switch (cabinData.crew()) {
                case SINGLE_HUMAN -> "/textures/sprites/crew_human_1.png";
                case DOUBLE_HUMAN -> "/textures/sprites/crew_human_2.png";
                case PURPLE_ALIEN -> "/textures/sprites/crew_purple_alien.png";
                case BROWN_ALIEN -> "/textures/sprites/crew_brown_alien.png";

                default -> throw new IllegalStateException("Unexpected value: " + cabinData.crew());
            };

            ImageView overlay = new ImageView(new Image(Objects.requireNonNull(ShipHandler.class.getResourceAsStream(imagePath))));

            overlay.setPreserveRatio(true);
            overlay.setCache(true);

            overlay.setFitWidth(cellSize);
            overlay.setFitHeight(cellSize);

            overlay.setCursor(Cursor.HAND);
            overlay.setPickOnBounds(true);
            overlay.setOnMouseClicked(_ -> VirtualServer.getInstance().sendMessage(new RemoveMemberMessage(new Point(cabinData.x(), cabinData.y()))));

            crewGrid.add(overlay, cabinData.x(), cabinData.y());
        }
    }

    public static void updateBatteriesPane(GridPane batteriesGrid, Set<ShipBoard.BatteriesData> batteries, double cellSize) {
        batteriesGrid.getChildren().clear();

        for (ShipBoard.BatteriesData batteriesData : batteries) {
            Node node;

            if (batteriesData.batteries() != batteriesData.capacity()) {
                // Add overlay image if battery not full
                String imagePath = "/textures/sprites/batteries_" + batteriesData.capacity() + "_" + batteriesData.batteries() + ".png";

                ImageView overlay = new ImageView(new Image(Objects.requireNonNull(ShipHandler.class.getResourceAsStream(imagePath))));

                overlay.setPreserveRatio(true);
                overlay.setCache(true);
                overlay.setRotate(batteriesData.rotation() * 90);
                overlay.setFitWidth(cellSize);
                overlay.setFitHeight(cellSize);

                node = overlay;
            } else {
                // Transparent clickable region
                Region transparentOverlay = new Region();
                transparentOverlay.setPrefSize(cellSize, cellSize);
                transparentOverlay.setStyle("-fx-background-color: transparent;");

                node = transparentOverlay;
            }

            if(batteriesData.batteries() > 0) {
                node.setCursor(Cursor.HAND);
                node.setPickOnBounds(true);
                node.setOnMouseClicked(_ -> {
                    batteriesGrid.setMouseTransparent(true);
                    VirtualServer.getInstance().sendMessage(new BatteryUseMessage(new Point(batteriesData.x(), batteriesData.y())));
                });
            }

            batteriesGrid.add(node, batteriesData.x(), batteriesData.y());
        }
    }
}
