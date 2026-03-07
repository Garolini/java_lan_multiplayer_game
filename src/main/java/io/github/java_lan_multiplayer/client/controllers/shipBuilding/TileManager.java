package io.github.java_lan_multiplayer.client.controllers.shipBuilding;

import io.github.java_lan_multiplayer.common.messages.shipBuilding.*;
import io.github.java_lan_multiplayer.server.model.BoardType;
import io.github.java_lan_multiplayer.client.ClientSession;
import io.github.java_lan_multiplayer.client.VirtualServer;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.model.ShipBoard;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TileManager {

    private final AnchorPane mainContentPane;

    private final TileEffectManager tileEffectManager = new TileEffectManager();

    public static final String TILES_PATH = "/textures/tiles/";
    private static final double TILE_SIZE = 83.5;

    private static final int TILES_AREA_MIN_X = 712;
    private static final int TILES_AREA_MAX_X = 1300;
    private static final int TILES_AREA_MIN_Y = 170;
    private static final int TILES_AREA_MAX_Y = 700;

    private final GridPane gridPane;
    private final Rectangle focusSlot;
    private final StackPane reservedSlot0;
    private final StackPane reservedSlot1;

    private ImageView selectedTile = null;
    private final ImageView[] reservedTiles = new ImageView[2];

    private static final Set<StackPane> validCells = new HashSet<>();

    private Integer gridX = null, gridY = null;

    private final VirtualServer virtualServer = VirtualServer.getInstance();

    public TileManager(AnchorPane mainContentPane, GridPane gridPane, Rectangle focusSlot, StackPane reservedSlot0, StackPane reservedSlot1) {
        this.mainContentPane = mainContentPane;
        this.gridPane = gridPane;
        this.focusSlot = focusSlot;
        this.reservedSlot0 = reservedSlot0;
        this.reservedSlot1 = reservedSlot1;
    }

    public ImageView generateTile(int id, Image backTexture) {

        ImageView tile = new ImageView(backTexture);
        tile.setId(String.valueOf(id));
        tile.setFitWidth(TILE_SIZE);
        tile.setFitHeight(TILE_SIZE);
        tile.setCursor(Cursor.HAND);
        tile.setCache(true);

        tileEffectManager.applyDropShadowEffect(tile, false);

        double randomX = Math.random() * (TILES_AREA_MAX_X - TILES_AREA_MIN_X) + TILES_AREA_MIN_X;
        double randomY = Math.random() * (TILES_AREA_MAX_Y - TILES_AREA_MIN_Y) + TILES_AREA_MIN_Y;
        tile.setLayoutX(randomX);
        tile.setLayoutY(randomY);

        tile.setTranslateX(1500 - tile.getLayoutX() * .7);
        tile.setTranslateY(100 - tile.getLayoutY() * .7);
        tile.setScaleX(1.5);
        tile.setScaleY(1.5);
        tile.setOpacity(0.4);

        final double[] offsetX = new double[1];
        final double[] offsetY = new double[1];
        final double[] pressX = new double[1];
        final double[] pressY = new double[1];

        tile.setOnMousePressed(event -> {
            tile.toFront();
            offsetX[0] = event.getSceneX() - tile.getLayoutX();
            offsetY[0] = event.getSceneY() - tile.getLayoutY();
            pressX[0] = event.getSceneX();
            pressY[0] = event.getSceneY();
        });

        tile.setOnMouseDragged(event -> {
            tile.setLayoutX(event.getSceneX() - offsetX[0]);
            tile.setLayoutY(event.getSceneY() - offsetY[0]);

            if (tile.getLayoutX() < TILES_AREA_MIN_X) tile.setLayoutX(TILES_AREA_MIN_X);
            if (tile.getLayoutX() > TILES_AREA_MAX_X) tile.setLayoutX(TILES_AREA_MAX_X);
            if (tile.getLayoutY() < TILES_AREA_MIN_Y) tile.setLayoutY(TILES_AREA_MIN_Y);
            if (tile.getLayoutY() > TILES_AREA_MAX_Y) tile.setLayoutY(TILES_AREA_MAX_Y);
        });

        tile.setOnMouseReleased(event -> {
            double deltaX = event.getSceneX() - pressX[0];
            double deltaY = event.getSceneY() - pressY[0];
            if (Math.hypot(deltaX, deltaY) < 5) {
                handlePreviousSelectedTile();
                virtualServer.sendMessage(new TileTakenMessage(null, Integer.parseInt(tile.getId())));
            }
        });
        return tile;
    }

    public void initializeGridPane(BoardType shipType) {
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 7; x++) {
                if (isCellValid(shipType, x, y)) {

                    StackPane cell = new StackPane();
                    cell.setStyle("-fx-background-color: rgba(255, 255, 255, .2); -fx-background-radius: 5");
                    cell.setOpacity(0);

                    gridPane.add(cell, x, y);

                    cell.setOnMouseClicked(_ -> {

                        if (selectedTile == null || !validCells.contains(cell)) return;

                        int colIndex = GridPane.getColumnIndex(cell) != null ? GridPane.getColumnIndex(cell) : 0;
                        int rowIndex = GridPane.getRowIndex(cell) != null ? GridPane.getRowIndex(cell) : 0;

                        Point2D cellCenter = getNodeCenterIn(cell, mainContentPane);

                        handleSlotClicked(colIndex, rowIndex, cellCenter);
                    });
                }
            }
        }
        // initialize with the center cabin tile
        updateValidCells(3, 2);

        String color = ClientSession.getPlayerInfo().getColor().toLowerCase();
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(TILES_PATH + "tile_starter_cabin_" + color + ".png")));
        ImageView starterCabin = new ImageView(image);
        starterCabin.setFitWidth(TILE_SIZE);
        starterCabin.setFitHeight(TILE_SIZE);
        gridPane.add(starterCabin, 3, 2);
        starterCabin.setTranslateX(-400);
        starterCabin.setTranslateY(100);
        starterCabin.setRotate(Math.random() * 360 - 180);
        ShipAnim.playStarterCabinAnimation(starterCabin, 1200);
    }

    public void takeTile(ImageView tile) {

        selectedTile = tile;

        // move a tile from the choosing pit to the Focus spot
        moveSelectedToFocusSlot(true);

        // disable mouse events on the imageView in the Focus spot
        selectedTile.setDisable(true);
    }

    public void handlePreviousSelectedTile() {

        if(selectedTile == null) return;

        if(gridX == null || gridY == null) {
            handleUnplacedTile();
        }
        else if(gridY == -1) {
            handleReservedTile();
        }
        else {
            handlePlacedTile();
        }

        selectedTile.setDisable(true);
        focusSlot.setDisable(true);
        focusSlot.setCursor(Cursor.DEFAULT);
        selectedTile = null;
    }

    private void handleUnplacedTile() {
        // tile was not placed on the grid
        if(isHoldingReservedTile()) {
            // if it is a reserved tile, it returns to its slot
            moveReservedTile();
        }
        else {
            // otherwise it puts it back to the table
            selectedTile.setMouseTransparent(false);
            virtualServer.sendMessage(new TileReturnedMessage());
        }
    }
    private void handleReservedTile() {
        // saves the tile to reserved tile
        if(gridX < 0 || gridX > 1) {
            Logger.logError("Out of bounds index for selected tile: " + gridX);
            return;
        }
        removeTileFromReservedSlots();
        reservedTiles[gridX] = selectedTile;
        tileEffectManager.applyReservedEffect(selectedTile);
        virtualServer.sendMessage(new ReserveTileMessage(gridX));
    }
    private void handlePlacedTile() {
        // fixes the tile to the ship
        selectedTile.setEffect(null);
        int degrees = (int)selectedTile.getRotate();
        int rotation = ((degrees / 90) % 4 + 4) % 4;

        removeTileFromReservedSlots();
        updateValidCells(gridX, gridY);
        virtualServer.sendMessage(new TileFixedMessage(gridX, gridY, rotation));
        gridX = gridY = null;
    }

    public void moveSelectedToFocusSlot(boolean flipTile) {
        gridX = gridY = null;
        if(selectedTile == null) return;
        tileEffectManager.applyDropShadowEffect(selectedTile, isHoldingReservedTile());
        showValidCells();
        focusSlot.setDisable(true);
        focusSlot.setCursor(Cursor.DEFAULT);
        Point2D focusSlotCenter = getNodeCenterIn(focusSlot, mainContentPane);
        ShipAnim.animateTileToPosition(selectedTile, mainContentPane, focusSlotCenter, 1.7, 0,500, 500, flipTile);
    }

    private boolean isCellValid(BoardType shipType, int x, int y) {
        if(y < 0 || x < 0 || y >= 5 || x >= 7) return false;
        return ShipBoard.isCellValid(x, y, shipType);
    }

    public void rotateSelectedTile(double angle) {
        ShipAnim.rotateTile(selectedTile, angle);
    }

    public void updateValidCells(int x, int y) {
        for (Node node : gridPane.getChildren()) {
            if (node instanceof StackPane cell) {
                int nodeCol = GridPane.getColumnIndex(cell) != null ? GridPane.getColumnIndex(cell) : 0;
                int nodeRow = GridPane.getRowIndex(cell) != null ? GridPane.getRowIndex(cell) : 0;

                boolean isAdjacentHorizontally = Math.abs(nodeCol - x) == 1 && nodeRow == y;
                boolean isAdjacentVertically = Math.abs(nodeRow - y) == 1 && nodeCol == x;

                if (isAdjacentHorizontally || isAdjacentVertically) {
                    validCells.add(cell);
                }
            }
        }
        removePaneFromGrid(x, y);
    }

    public void showValidCells() {
        validCells.forEach(ShipAnim::playShowSlot);
        ShipAnim.playShowSlot(reservedSlot0);
        ShipAnim.playShowSlot(reservedSlot1);
    }
    public void hideValidCells() {
        validCells.forEach(ShipAnim::playHideSlot);
        ShipAnim.playHideSlot(reservedSlot0);
        ShipAnim.playHideSlot(reservedSlot1);
    }

    private void removePaneFromGrid(int x, int y) {
        Node nodeToRemove = null;

        // loop through the Panes inside the gidPane till it finds the one corresponding to col,row values
        for (Node node : gridPane.getChildren()) {
            if (node instanceof StackPane cell) {
                int cellCol = GridPane.getColumnIndex(cell) != null ? GridPane.getColumnIndex(cell) : 0;
                int cellRow = GridPane.getRowIndex(cell) != null ? GridPane.getRowIndex(cell) : 0;

                if (cellCol == x && cellRow == y) {
                    nodeToRemove = cell;
                    break;
                }
            }
        }
        if (nodeToRemove != null) {
            validCells.remove(nodeToRemove);
            gridPane.getChildren().remove(nodeToRemove);
        }
    }

    private boolean isHoldingReservedTile() {
        if(selectedTile == null) return false;
        for(ImageView reservedTile : reservedTiles) {
            if(reservedTile != null && reservedTile == selectedTile) return true;
        }
        return false;
    }

    public void onReservedSlotClick(Event event) {

        Node slotNode = (Node) event.getSource();
        int slotId = Integer.parseInt((slotNode).getId());


        if(selectedTile != null && (reservedTiles[slotId] == null || reservedTiles[slotId] == selectedTile)) {
            // moves the selected tile to the reserved slot.

            handleSlotClicked(slotId, -1, getNodeCenterIn(slotNode, mainContentPane));
        }
        else if(reservedTiles[slotId] != null && reservedTiles[slotId] != selectedTile) {
            handlePreviousSelectedTile();
            selectedTile = reservedTiles[slotId];
            virtualServer.sendMessage(new UseReservedTileMessage(slotId));
            moveSelectedToFocusSlot(false);
        }
    }

    private void handleSlotClicked(int colIndex, int rowIndex, Point2D slotCenter) {

        if (gridX != null && gridY != null && gridX == colIndex && gridY == rowIndex) {
            moveSelectedToFocusSlot(false);
            return;
        }

        gridX = colIndex;
        gridY = rowIndex;

        // moving tile from Focus spot to the reserved slot
        ShipAnim.animateTileToPosition(selectedTile, mainContentPane, slotCenter, 1, selectedTile.getRotate(), 250, 400, false);

        tileEffectManager.applySelectedEffect(selectedTile, isHoldingReservedTile());

        focusSlot.setDisable(false);
        focusSlot.setCursor(Cursor.HAND);
        selectedTile.setMouseTransparent(true);
    }

    private void moveReservedTile() {
        if(selectedTile == null) return;

        Node slot = null;
        if(reservedTiles[0] != null && reservedTiles[0] == selectedTile) {
            slot = reservedSlot0;
        } else if(reservedTiles[1] != null && reservedTiles[1] == selectedTile) {
            slot = reservedSlot1;
        }
        if(slot == null) {
            Logger.logWarning("Reserved tile " + selectedTile.getId() + " is already in the grid");
            return;
        }
        virtualServer.sendMessage(new ReserveTileMessage(Integer.parseInt(slot.getId())));
        Point2D slotCenter = getNodeCenterIn(slot, mainContentPane);
        ShipAnim.animateTileToPosition(selectedTile, mainContentPane, slotCenter, 1, selectedTile.getRotate(), 250, 400, false);
    }

    private void removeTileFromReservedSlots() {
        for(int slotId = 0; slotId < reservedTiles.length; slotId++) {
            if(reservedTiles[slotId] != null && reservedTiles[slotId] == selectedTile) {
                reservedTiles[slotId] = null;
            }
        }
    }

    public static Point2D getNodeCenterIn(Node node, Node relativeTo) {
        Bounds boundsInScene = node.localToScene(node.getBoundsInLocal());
        Bounds boundsInTarget = relativeTo.sceneToLocal(boundsInScene);

        double centerX = boundsInTarget.getMinX() + boundsInTarget.getWidth() / 2;
        double centerY = boundsInTarget.getMinY() + boundsInTarget.getHeight() / 2;

        return new Point2D(centerX, centerY);
    }
    public static Point2D getRealCenterIn(Node node, Node relativeTo) {
        Bounds layoutBounds = node.getLayoutBounds();
        double centerX = layoutBounds.getMinX() + layoutBounds.getWidth() / 2;
        double centerY = layoutBounds.getMinY() + layoutBounds.getHeight() / 2;

        Point2D centerInLocal = new Point2D(centerX, centerY);
        Point2D centerInScene = node.localToScene(centerInLocal);

        return relativeTo.sceneToLocal(centerInScene);
    }
}