package io.github.java_lan_multiplayer.client.controllers.flight;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.java_lan_multiplayer.client.*;
import io.github.java_lan_multiplayer.common.messages.*;
import io.github.java_lan_multiplayer.common.messages.flight.*;
import io.github.java_lan_multiplayer.client.popups.PendingPopup;
import io.github.java_lan_multiplayer.client.popups.Popup;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.endGame.PlayersScoresMessage;
import io.github.java_lan_multiplayer.server.model.BoardType;
import io.github.java_lan_multiplayer.server.model.ShipBoard;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;


public class FlightController implements VirtualServerAware {

    private final VirtualServer virtualServer = VirtualServer.getInstance();

    @FXML private Label statusLabel;

    @FXML private StackPane gameBoardPane;
    @FXML private ImageView gameBoardImage;

    @FXML private Button settingsButton;

    @FXML private Button pickCardButton;

    @FXML private StackPane deckPane;
    @FXML private Region currentCardRegion;

    @FXML private StackPane shipboardP1;

    @FXML private ImageView shipImageP1, shipImageP2, shipImageP3, shipImageP4;

    @FXML private GridPane shipGridP1, shipGridP2, shipGridP3, shipGridP4;

    @FXML private GridPane batteriesGridP1, batteriesGridP2, batteriesGridP3, batteriesGridP4;
    @FXML private GridPane crewGridP1, crewGridP2, crewGridP3, crewGridP4;
    @FXML private GridPane cargoGridP1, cargoGridP2, cargoGridP3, cargoGridP4;

    @FXML private Label creditsLabelP1, creditsLabelP2, creditsLabelP3, creditsLabelP4;

    @FXML private HBox nameBoxP2, nameBoxP3, nameBoxP4;

    @FXML private VBox P1Box, P2Box, P3Box, P4Box;

    @FXML private Button doneButton;

    CardsHandler cardsHandler;

    private final Map<String, StackPane> shipMap = new HashMap<>();
    private final Map<String, GridPane> tilesGridMap = new HashMap<>();
    private final Map<String, GridPane> batteriesGridMap = new HashMap<>();
    private final Map<String, GridPane> crewGridMap = new HashMap<>();
    private final Map<String, GridPane> cargoGridMap = new HashMap<>();
    private final Map<String, VBox> shipBoxMap = new HashMap<>();
    private final Map<String, Label> creditsLabelMap = new HashMap<>();

    private final Map<GridPane, Integer> gridCellsSize = new HashMap<>();

    public void initialize() {
        cardsHandler = new CardsHandler(batteriesGridP1, shipboardP1, deckPane, currentCardRegion, pickCardButton, tilesGridMap, gridCellsSize);
        ClientSession.setGivingUp(false);
        assignPlayerGrids(ClientSession.getPlayerNames());
        FlightAnim.generatePlayerPawns(ClientSession.getPlayerNames(), gameBoardPane);
        setGridSize(71, shipGridP1, batteriesGridP1, crewGridP1, cargoGridP1);
        setGridSize(28, shipGridP2, batteriesGridP2, crewGridP2, cargoGridP2,
                shipGridP3, batteriesGridP3, crewGridP3, cargoGridP3,
                shipGridP4, batteriesGridP4, crewGridP4, cargoGridP4);


        FlightAnim.initialize(this);
        FlightAnim.queueUpdateStatus(LanguageManager.get("status.validating_ship"), 3000);

        settingsButton.setOnAction(_ -> Popup.newSettingsPopup(false, true).show());
        doneButton.setOnAction(_ -> virtualServer.sendMessage(new SimpleMessage("done")));
        pickCardButton.setOnAction(_ -> virtualServer.sendMessage(new SimpleMessage("pick_card")));

        initHandlers();
    }

    @Override
    public void updateCallBack() {
        virtualServer.setCallBack(cmd -> {
            ObjectMapper mapper = new ObjectMapper();
            String type = mapper.readTree(cmd).get("type").asText();
            switch (type) {

                case "players_scores" -> virtualServer.setCallBack(null);

                case "game_reset" -> {
                    GameResetMessage message = mapper.readValue(cmd, GameResetMessage.class);

                    virtualServer.setCallBack(null);
                    PendingPopup.set(Popup.newAlertPopup("title.player_left", "description.player_left", message.getPlayerName()));
                    Platform.runLater(() -> SceneSwitch.switchScene("LobbyScene"));
                    return;
                }
            }
            safeUpdateView(cmd);
        });
    }

    private void safeUpdateView(String cmd) {
        Platform.runLater(() -> updateView(cmd));
    }

    ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Consumer<String>> handlers = new HashMap<>();

    private void initHandlers() {
        handlers.put("game_state",        handleAs(GameStateMessage.class, this::handleGameState));
        handlers.put("board_type",        handleAs(BoardTypeMessage.class, this::handleBoardType));
        handlers.put("ship_board",        handleAs(ShipBoardMessage.class, this::handleShipBoard));
        handlers.put("player_position",   handleAs(PlayerPositionMessage.class, this::handlePlayerPosition));
        handlers.put("batteries",         handleAs(BatteriesMessage.class, this::handleBatteries));
        handlers.put("crew",              handleAs(CrewMessage.class, this::handleCrew));
        handlers.put("cargo",             handleAs(CargoMessage.class, this::handleCargo));
        handlers.put("credits",           handleAs(CreditsMessage.class, this::handleCredits));
        handlers.put("invalid_tiles",     handleAs(TilesMessage.class, this::handleInvalidTiles));
        handlers.put("alien_cabin_tiles", handleAs(TilesMessage.class, this::handleAlienCabinTiles));
        handlers.put("pick_available",    handleAs(PickAvailableMessage.class, this::handlePickAvailable));
        handlers.put("player_eliminated", handleAs(SimplePlayerMessage.class, this::handlePlayerEliminated));
        handlers.put("players_scores",    handleAs(PlayersScoresMessage.class, this::handlePlayersScores));
    }

    private <T> Consumer<String> handleAs(Class<T> type, Consumer<T> handler) {
        return json -> {
            try {
                T msg = mapper.readValue(json, type);
                handler.accept(msg);
            } catch (JsonProcessingException e) {
                Logger.logError("Failed to parse " + type.getSimpleName() + ": " + e.getMessage());
            }
        };
    }

    private void updateView(String cmd) {
        try {
            String type = mapper.readTree(cmd).get("type").asText();
            Consumer<String> handler = handlers.get(type);

            if (handler != null) {
                handler.accept(cmd);
            } else {
                cardsHandler.handleCardCommand(type, cmd);
            }
        } catch (JsonProcessingException e) {
            Logger.logError("Failed to parse command JSON: " + e.getMessage());
        }
    }

    private void handleGameState(GameStateMessage message) {

        switch (message.getGameState()) {

            case ASSIGNING_CREW -> {}
            case PICKING_CARDS -> doneButton.setVisible(false);

            default -> throw new IllegalStateException("Invalid game state");
        }
        Logger.logDebug("new state: " + message.getGameState());
    }

    private void handleBoardType(BoardTypeMessage message) {
        BoardType boardType = message.getBoardType();

        updateShipTypes(boardType);
        updateGameBoardType(boardType);
        FlightAnim.generateGameBoardSlots(boardType);
    }

    private void updateShipTypes(BoardType boardType) {
        String boardPath = switch (boardType) {
            case LEARNING, LEVEL_ONE -> "/textures/cardboards/shipboard_1.png";
            case LEVEL_TWO -> "/textures/cardboards/shipboard_2.png";
        };
        Image ship = new Image(Objects.requireNonNull(getClass().getResourceAsStream(boardPath)));

        List<ImageView> shipImages = List.of(shipImageP1, shipImageP2, shipImageP3, shipImageP4);
        for (ImageView imageView : shipImages) {
            imageView.setImage(ship);

            FadeTransition boardFade = new FadeTransition(Duration.millis(1500), imageView);
            boardFade.setFromValue(0);
            boardFade.setToValue(1);
            boardFade.play();
        }
    }

    private void updateGameBoardType(BoardType boardType) {
        String boardPath = switch (boardType) {
            case LEARNING  -> "/textures/cardboards/cardboard_L.png";
            case LEVEL_ONE -> "/textures/cardboards/cardboard_1.png";
            case LEVEL_TWO -> "/textures/cardboards/cardboard_2.png";
        };
        Image ship = new Image(Objects.requireNonNull(getClass().getResourceAsStream(boardPath)));

            gameBoardImage.setImage(ship);

            FadeTransition boardFade = new FadeTransition(Duration.millis(1500), gameBoardImage);
            boardFade.setFromValue(0);
            boardFade.setToValue(1);
            boardFade.play();
    }

    private void handleShipBoard(ShipBoardMessage message) {
        Set<TileInfo> tiles = message.getTiles();
        String playerName = message.getPlayerName();
        String color = ClientSession.getPlayerInfo(playerName).getColor();

        GridPane grid = tilesGridMap.get(playerName);

        if (grid != null) {
            if(grid == shipGridP1) FlightAnim.unmarkTiles(grid);
            ShipHandler.updateShipPane(grid, tiles, color, gridCellsSize.get(grid));
        } else {
            Logger.logError("No grid assigned for player: " + playerName);
        }
    }

    private void handlePlayerPosition(PlayerPositionMessage message) {
        FlightAnim.movePlayerPawn(message.getPlayerName(), message.getNewPosition(), gameBoardImage);
    }

    private void handleBatteries(BatteriesMessage message) {
        Set<ShipBoard.BatteriesData> batteriesData = message.getBatteries();
        String playerName = message.getPlayerName();

        GridPane grid = batteriesGridMap.get(playerName);

        if (grid != null) {
            ShipHandler.updateBatteriesPane(grid, batteriesData, gridCellsSize.get(grid));
        } else {
            Logger.logError("No batteries grid assigned for player: " + playerName);
        }
    }

    private void handleCrew(CrewMessage message) {
        Set<ShipBoard.CrewData> crewData = message.getCrew();
        String playerName = message.getPlayerName();

        GridPane grid = crewGridMap.get(playerName);

        if (grid != null) {
            ShipHandler.updateCrewPane(grid, crewData, gridCellsSize.get(grid));
            //FlightAnim.queueCrewPaneUpdate(grid, crewData);
        } else {
            Logger.logError("No crew grid assigned for player: " + playerName);
        }
    }

    private void handleCargo(CargoMessage message) {
        Set<ShipBoard.CargoData> crewData = message.getCargo();
        String playerName = message.getPlayerName();

        GridPane grid = cargoGridMap.get(playerName);

        if (grid != null) {
            cardsHandler.updateCargoPane(grid, crewData, gridCellsSize.get(grid));
        } else {
            Logger.logError("No crew grid assigned for player: " + playerName);
        }
    }

    private void handleCredits(CreditsMessage message) {

        String playerName = message.getPlayerName();

        Label label = creditsLabelMap.get(playerName);

        if (label != null) {
            label.setText(message.getCredits() + " ¢");
        } else {
            Logger.logError("No label assigned for player: " + playerName);
        }
    }

    private void handleInvalidTiles(TilesMessage message) {
        String playerName = message.getPlayerName();
        if(!ClientSession.isSelf(playerName)) {
            Logger.logError("Should only receive its own invalid tiles");
            return;
        }
        if(message.getTiles().isEmpty()) {
            FlightAnim.queueUpdateStatus(LanguageManager.get("status.valid_ship"), 800);
            return;
        }
        FlightAnim.queueUpdateStatus(LanguageManager.get("status.invalid_ship"), 800);
        for(Node node : shipGridP1.getChildren()) {
            if(!(node instanceof ImageView tile)) continue;
            int x = GridPane.getColumnIndex(node);
            int y = GridPane.getRowIndex(node);
            if(setContainsPoint(x, y, message.getTiles())) {
                FlightAnim.markTile(tile, new Color(1, 0, 0, .5));
                tile.setOnMouseClicked(_ -> virtualServer.sendMessage(new SelectedTileMessage(x, y)));
            }
        }
    }

    private void handleAlienCabinTiles(TilesMessage message) {
        String playerName = message.getPlayerName();
        if(!ClientSession.isSelf(playerName)) {
            Logger.logError("Should only receive its own invalid tiles");
            return;
        }
        if(message.getTiles().isEmpty()) {
            FlightAnim.queueUpdateStatus(LanguageManager.get("status.no_alien_cabin"), 800);
            if(doneButton.isVisible()) doneButton.setDisable(true);
            FlightAnim.unmarkTiles(shipGridP1);
            return;
        }
        FlightAnim.queueUpdateStatus(LanguageManager.get("status.choose_cabin"), 800);
        doneButton.setVisible(true);
        for(Node node : shipGridP1.getChildren()) {
            if(!(node instanceof ImageView tile)) continue;
            int x = GridPane.getColumnIndex(node);
            int y = GridPane.getRowIndex(node);
            if(setContainsPoint(x, y, message.getTiles())) {
                FlightAnim.markTile(tile, new Color(1, 1, 1, .5));
                tile.setOnMouseClicked(_ -> virtualServer.sendMessage(new SelectedTileMessage(x, y)));
            }
        }
    }

    private void handlePickAvailable(PickAvailableMessage message) {

        String pickerName = message.getPlayerName();
        boolean isSelf = ClientSession.isSelf(pickerName);

        if(isSelf) {
            FlightAnim.queueEnablePickButton(pickCardButton);
        } else {
            FlightAnim.queueUpdateStatus(LanguageManager.get("status.waiting_pick", pickerName), 800);
        }
    }

    private void handlePlayerEliminated(SimplePlayerMessage message) {

        String playerName = message.getPlayerName();
        VBox playerBox = shipBoxMap.get(playerName);

        FlightAnim.queuePlayerEliminated(playerName, playerBox);
    }

    private void handlePlayersScores(PlayersScoresMessage message) {

        FlightAnim.queueGameOver(message.getPlayersScores());
    }

    private boolean setContainsPoint(int x, int y, Set<Point> points) {
        for(Point point : points) {
            if(point.x == x && point.y == y) return true;
        }
        return false;
    }


    private void assignPlayerGrids(Set<String> playerNames) {
        tilesGridMap.clear();

        // Always assign self to shipGridP1
        String self = ClientSession.getUsername();

        tilesGridMap.put(self, shipGridP1);
        batteriesGridMap.put(self, batteriesGridP1);
        crewGridMap.put(self, crewGridP1);
        cargoGridMap.put(self, cargoGridP1);
        creditsLabelMap.put(self, creditsLabelP1);
        shipBoxMap.put(self, P1Box);

        // Assign other players to P2, P3, P4
        List<GridPane> otherGrids = List.of(shipGridP2, shipGridP3, shipGridP4);
        List<GridPane> otherBatteriesGrids = List.of(batteriesGridP2, batteriesGridP3, batteriesGridP4);
        List<GridPane> otherCrewGrids = List.of(crewGridP2, crewGridP3, crewGridP4);
        List<GridPane> otherCargoGrids = List.of(cargoGridP2, cargoGridP3, cargoGridP4);
        List<Label> otherCreditsLabels = List.of(creditsLabelP2, creditsLabelP3, creditsLabelP4);
        List<HBox> nameBoxes = List.of(nameBoxP2, nameBoxP3, nameBoxP4);
        List<VBox> shipBoxes = List.of(P2Box, P3Box, P4Box);
        int i = 0;

        for (String name : playerNames) {
            if (!name.equals(self)) {
                if (i >= otherGrids.size()) {
                    Logger.logWarning("Too many players for available grids");
                    return;
                }
                tilesGridMap.put(name, otherGrids.get(i));
                batteriesGridMap.put(name, otherBatteriesGrids.get(i));
                crewGridMap.put(name, otherCrewGrids.get(i));
                cargoGridMap.put(name, otherCargoGrids.get(i));
                creditsLabelMap.put(name, otherCreditsLabels.get(i));
                shipBoxMap.put(name, shipBoxes.get(i));
                updateNameBox(nameBoxes.get(i), name);
                shipBoxes.get(i).setVisible(true);
                i++;
            }
        }
    }

    private void updateNameBox(HBox nameBox, String name) {
        nameBox.getChildren().clear();

        StackPane profilePic = ClientSession.getPlayerProfilePicture(name, 30);
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("white-label");
        nameLabel.setStyle("-fx-font-weight: bold");
        nameBox.getChildren().addAll(profilePic, nameLabel);
    }

    private void setGridSize(int size, GridPane... panes) {
        for (GridPane pane : panes) {
            gridCellsSize.put(pane, size);
        }
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public StackPane getShipboardP1() {
        return shipboardP1;
    }

    public GridPane getBatteriesGridP1() {
        return batteriesGridP1;
    }

    public GridPane getCrewGridP1() {
        return crewGridP1;
    }

    public GridPane getCargoGridP1() {
        return cargoGridP1;
    }

    public VBox getP1Box() {
        return P1Box;
    }

    public Integer getGridCellsSize(GridPane gridPane) {
        return gridCellsSize.get(gridPane);
    }
}