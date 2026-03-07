package io.github.java_lan_multiplayer.client.controllers.flight;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.java_lan_multiplayer.client.ClientSession;
import io.github.java_lan_multiplayer.client.LanguageManager;
import io.github.java_lan_multiplayer.client.SceneSwitch;
import io.github.java_lan_multiplayer.client.popups.PendingPopup;
import io.github.java_lan_multiplayer.client.popups.Popup;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.endGame.PlayerScores;
import io.github.java_lan_multiplayer.common.messages.flight.TileInfo;
import io.github.java_lan_multiplayer.server.model.BoardType;
import io.github.java_lan_multiplayer.server.model.ShipBoard;
import io.github.java_lan_multiplayer.server.model.cards.projectile.Projectile;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class FlightAnim {

    private static final Queue<Runnable> animationQueue = new LinkedList<>();
    private static boolean isAnimationPlaying = false;

    private static final Map<Integer, Point> boardSlotCoordinates = new HashMap<>();
    private static final Map<String, StackPane> playerPawns = new HashMap<>();

    private static FlightController controller;

    private static Popup currentPopup;

    private static void playNextAnimation() {
        if (isAnimationPlaying || animationQueue.isEmpty()) return;

        Runnable next = animationQueue.poll();
        isAnimationPlaying = true;
        next.run();
    }

    public static void startNextAnimation() {
        isAnimationPlaying = false;
        playNextAnimation();
    }

    private static void pauseBeforeNextAnimation(double duration) {
        PauseTransition pause = new PauseTransition(Duration.millis(duration));
        pause.setOnFinished(_ -> startNextAnimation());
        pause.play();
    }

    public static void initialize(FlightController controller) {
        FlightAnim.controller = controller;
    }

    public static void queueUpdateStatus(String message, double duration) {
        animationQueue.offer(() -> updateStatus(message, duration));
        playNextAnimation();
    }
    private static void updateStatus(String message, double duration) {
        Label statusLabel = controller.getStatusLabel();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), statusLabel);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(_ -> statusLabel.setText(message));

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), statusLabel);
        fadeIn.setToValue(1);

        SequentialTransition animation = new SequentialTransition(fadeOut, fadeIn);
        animation.setOnFinished(_ -> pauseBeforeNextAnimation(duration));
        animation.play();
    }

    public static void queueEnablePickButton(Button pickCardButton) {
        animationQueue.offer(() -> enablePickButton(pickCardButton));
        playNextAnimation();
    }
    private static void enablePickButton(Button pickCardButton) {
        pickCardButton.setDisable(false);
        updateStatus(LanguageManager.get("status.pick_card"), 800);
    }

    public static void movePlayerPawn(String playerName, int position, ImageView gameBoardImage) {
        double originalWidth = 1055; // Width of the original PNG
        double scale = gameBoardImage.getFitWidth() / originalWidth;

        StackPane pawn = playerPawns.get(playerName);

        int trackLength = boardSlotCoordinates.size();
        int newPosition = (position % trackLength + trackLength) % trackLength;
        double gameBoardX = gameBoardImage.getLayoutX();
        double gameBoardY = gameBoardImage.getLayoutY();
        double newX = gameBoardX + boardSlotCoordinates.get(newPosition).x * scale - pawn.getPrefWidth() / 2;
        double newY = gameBoardY + boardSlotCoordinates.get(newPosition).y * scale - pawn.getPrefHeight() / 2;

        queuePawnMove(pawn, newX, newY);
    }

    private static void queuePawnMove(StackPane pawn, double x, double y) {
        animationQueue.offer(() -> playPawnMove(pawn, x, y));
        playNextAnimation();
    }

    private static void playPawnMove(StackPane pawn, double x, double y) {

        TranslateTransition move = new TranslateTransition(Duration.millis(500), pawn);
        move.setToX(x - pawn.getLayoutX());
        move.setToY(y - pawn.getLayoutY());

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), pawn);
        fadeOut.setToValue(.7);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), pawn);
        scaleOut.setToX(1.2);
        scaleOut.setToY(1.2);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), pawn);
        fadeIn.setDelay(Duration.millis(350));
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), pawn);
        scaleIn.setDelay(Duration.millis(350));
        scaleIn.setToX(1);
        scaleIn.setToY(1);

        ParallelTransition animation = new ParallelTransition(move, fadeOut, scaleOut, fadeIn, scaleIn);
        animation.setOnFinished(_ -> startNextAnimation());
        animation.play();
    }

    public static void setShadowToCard(ImageView card) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(new Color(0, 0, 0, .5));
        shadow.setWidth(100);
        shadow.setHeight(100);
        shadow.setRadius(20);
        card.setEffect(shadow);
    }

    public static void markTile(ImageView tile, Color color) {
        tile.setCursor(javafx.scene.Cursor.HAND);
        InnerShadow shadow = new InnerShadow();
        shadow.setColor(color);
        shadow.setBlurType(BlurType.THREE_PASS_BOX);
        shadow.setWidth(50);
        shadow.setHeight(50);
        shadow.setRadius(10);
        shadow.setChoke(1.0);
        tile.setEffect(shadow);
    }

    public static void unmarkTiles(GridPane shipGridP1) {
        for(Node node : shipGridP1.getChildren()) {
            if(node instanceof ImageView tile) {
                tile.setEffect(null);
                tile.setOnMouseClicked(null);
                tile.setCursor(Cursor.DEFAULT);
            }
        }
    }

    public static void queueCardTaken(ImageView card, StackPane deckPane, Node target, Image newImage) {
        animationQueue.offer(() -> playCardTaken(card, deckPane, target, newImage));
        playNextAnimation();
    }

    private static void playCardTaken(ImageView card, StackPane deckPane, Node target, Image newImage) {
        final double duration = 600;

        card.toFront();

        // Convert bounds to deckPane's coordinate space
        Bounds targetBounds = deckPane.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        TranslateTransition translation = getCardTakenTransition(card, targetBounds);

        RotateTransition rotation = new RotateTransition(Duration.millis(duration), card);
        rotation.setToAngle(0);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(duration / 2), card);
        scaleIn.setToX(0);
        scaleIn.setToY(1.05);
        scaleIn.setOnFinished(_ -> card.setImage(newImage));

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(duration / 2), card);
        scaleOut.setDelay(Duration.millis(duration / 2));
        scaleOut.setToX(1.1);
        scaleOut.setToY(1.1);

        ParallelTransition animation = new ParallelTransition(translation, rotation, scaleIn, scaleOut);
        animation.setOnFinished(_ -> startNextAnimation());
        animation.play();
    }

    private static TranslateTransition getCardTakenTransition(ImageView card, Bounds targetBounds) {
        Bounds cardBounds = card.getBoundsInParent();

        // Calculate target center
        double targetCenterX = targetBounds.getMinX() + targetBounds.getWidth() / 2;
        double targetCenterY = targetBounds.getMinY() + targetBounds.getHeight() / 2;

        // Current card center
        double cardCenterX = cardBounds.getMinX() + cardBounds.getWidth() / 2;
        double cardCenterY = cardBounds.getMinY() + cardBounds.getHeight() / 2;

        // Difference to move the card to the target center
        double deltaX = targetCenterX - cardCenterX;
        double deltaY = targetCenterY - cardCenterY;

        TranslateTransition translation = new TranslateTransition(Duration.millis(600), card);
        translation.setByX(deltaX);
        translation.setByY(deltaY);
        return translation;
    }

    public static void playCardRemove(ImageView card) {
        final double duration = 600;

        setShadowToCard(card);

        TranslateTransition translation = new TranslateTransition(Duration.millis(duration), card);
        translation.setByX(-100);

        RotateTransition rotation = new RotateTransition(Duration.millis(duration), card);
        rotation.setByAngle(-70);

        FadeTransition fade = new FadeTransition(Duration.millis(duration), card);
        fade.setToValue(0);

        ParallelTransition animation = new ParallelTransition(translation, rotation, fade);
        animation.setOnFinished(_ -> {
            if (card.getParent() instanceof Pane pane) {
                pane.getChildren().remove(card);
            }
        });
        animation.play();
    }

    public static void queueDiceThrowPopup(String playerName, int[] dice) {
        animationQueue.offer(() -> showDiceThrowPopup(playerName, dice));
        playNextAnimation();
    }

    private static void showDiceThrowPopup(String playerName, int[] dice) {
        Popup.newDiceThrowPopup(playerName, dice[0], dice[1]).show();
        updateStatus(LanguageManager.get("label.rolling_dice", playerName), 5000);
    }

    public static void queueProjectile(ImageView projectile, Projectile.Source source, int pathIndex, Point hitPoint, boolean defended, Set<TileInfo> tiles, GridPane shipGrid, String color, int cellSize) {
        animationQueue.offer(() -> playProjectile(projectile, source, pathIndex, hitPoint, defended, tiles, shipGrid, color, cellSize));
        playNextAnimation();
    }

    private static void playProjectile(ImageView projectile, Projectile.Source source, int pathIndex, Point hitPoint, boolean defended, Set<TileInfo> tiles, GridPane shipGrid, String color, int cellSize) {
        int startX = 0, startY = 0;
        int dx = 0, dy = 0;
        int maxSteps = 0;
        int stepsToHit = -1;

        int clampedRow = Math.max(-1, Math.min(pathIndex, 5));
        int clampedCol = Math.max(-1, Math.min(pathIndex, 7));

        switch (source) {
            case UP:
                startX = (clampedCol - 3) * cellSize;
                startY = -cellSize * 3;
                dy = 1;
                maxSteps = 6;
                if (hitPoint != null) stepsToHit = hitPoint.y + 1;
                break;
            case DOWN:
                startX = (clampedCol - 3) * cellSize;
                startY = cellSize * 3;
                dy = -1;
                maxSteps = 6;
                if (hitPoint != null) stepsToHit = 4 - hitPoint.y + 1;
                break;
            case LEFT:
                startX = -cellSize * 4;
                startY = (clampedRow - 2) * cellSize;
                dx = 1;
                maxSteps = 8;
                if (hitPoint != null) stepsToHit = hitPoint.x + 1;
                break;
            case RIGHT:
                startX = cellSize * 4;
                startY = (clampedRow - 2) * cellSize;
                dx = -1;
                maxSteps = 8;
                if (hitPoint != null) stepsToHit = 6 - hitPoint.x + 1;
                break;
        }

        projectile.setTranslateX(startX);
        projectile.setTranslateY(startY);

        // Determine how far the projectile should travel
        double travelSteps;
        if (!defended) {
            travelSteps = (hitPoint != null) ? stepsToHit : maxSteps;
        } else {
            if (hitPoint == null) {
                travelSteps = 1; // Defended but no hit: explode early
            } else {
                travelSteps = stepsToHit - 0.5; // Bounce before the target
            }
        }

        double totalDuration = 2000;
        double duration = totalDuration * (travelSteps / (double) maxSteps);

        TranslateTransition transition = new TranslateTransition(Duration.millis(duration), projectile);
        transition.setByX(dx * travelSteps * cellSize);
        transition.setByY(dy * travelSteps * cellSize);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), projectile);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), projectile);
        fadeOut.setDelay(Duration.millis(Math.max(0, duration - 200)));
        fadeOut.setToValue(0);

        ParallelTransition animation = (!defended && hitPoint == null)? new ParallelTransition(fadeIn, transition, fadeOut) : new ParallelTransition(fadeIn, transition);
        final int finalDx = dx * cellSize;
        final int finalDy = dy * cellSize;
        animation.setOnFinished(_ -> {
            if (!defended) {
                if (hitPoint != null) {
                    playExplosion(projectile, true);
                    ShipHandler.updateShipPane(shipGrid, tiles, color, cellSize);
                } else {
                    ((Pane) projectile.getParent()).getChildren().remove(projectile);
                    pauseBeforeNextAnimation(500);
                }
            } else {
                if (hitPoint == null) {
                    // Early explosion
                    playExplosion(projectile, false);
                } else {
                    playBounce(projectile, finalDx, finalDy);
                }
            }
        });

        animation.play();
    }

    private static void playExplosion(ImageView projectile, boolean shipHit) {

        projectile.setImage(new Image(Objects.requireNonNull(FlightAnim.class.getResourceAsStream("/textures/sprites/explosion.png"))));

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), projectile);
        scale.setToX(shipHit? 1.3 : 1.1);

        RotateTransition rotate = new RotateTransition(Duration.millis(shipHit? 1000 : 800), projectile);
        rotate.setByAngle(180);

        FadeTransition fade = new FadeTransition(Duration.millis(300), projectile);
        fade.setDelay(Duration.millis(1000 - 300));
        fade.setToValue(0);

        ParallelTransition animation = new ParallelTransition(scale, rotate, fade);
        animation.setOnFinished(_ -> {
            ((Pane) projectile.getParent()).getChildren().remove(projectile);
            pauseBeforeNextAnimation(500);
        });
        animation.play();
    }

    private static void playBounce(ImageView projectile, int dx, int dy) {

        TranslateTransition move = new TranslateTransition(Duration.millis(1000), projectile);
        move.setByX(-dx);
        move.setByY(-dy);

        RotateTransition rotate = new RotateTransition(Duration.millis(1000), projectile);
        rotate.setByAngle(180);

        FadeTransition fade = new FadeTransition(Duration.millis(1000), projectile);
        fade.setToValue(0);

        ParallelTransition animation = new ParallelTransition(move, rotate, fade);
        animation.setOnFinished(_ -> {
            ((Pane) projectile.getParent()).getChildren().remove(projectile);
            pauseBeforeNextAnimation(500);
        });
        animation.play();
    }

    public static void queuePlayerEliminated(String playerName, VBox shipBox) {
        animationQueue.offer(() -> playPlayerEliminated(playerName, shipBox));
        playNextAnimation();
    }

    private static void playPlayerEliminated(String playerName, VBox shipBox) {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.4);
        colorAdjust.setSaturation(-0.8);

        shipBox.setEffect(colorAdjust);
        shipBox.setDisable(true);

        StackPane playerPawn = playerPawns.get(playerName);
        FadeTransition fade = new FadeTransition(Duration.millis(500), playerPawn);
        fade.setToValue(0);
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), playerPawn);
        scale.setToX(1.2);
        ParallelTransition animation = new ParallelTransition(fade, scale);
        animation.setOnFinished(_ -> ((Pane) playerPawn.getParent()).getChildren().remove(playerPawn));
        animation.play();

        if(ClientSession.isSelf(playerName)) {
            updateStatus(LanguageManager.get("status.eliminated"), 3000);
        } else {
            updateStatus(LanguageManager.get("status.player_eliminated", playerName), 3000);
        }
    }

    public static void queuePlanetsPopup(ImageView currentCard, List<String> planetChosenBy) {
        animationQueue.offer(() -> showPlanetsPopup(currentCard, planetChosenBy));
        playNextAnimation();
    }

    private static void showPlanetsPopup(ImageView currentCard, List<String> planetChosenBy) {
        Popup.newPlanetsCardPopup(currentCard.getImage(), planetChosenBy).show();

        pauseBeforeNextAnimation(500);
    }

    public static void queueCardDecisionPopup(ImageView currentCard) {
        animationQueue.offer(() -> showCardDecisionPopup(currentCard));
        playNextAnimation();
    }

    private static void showCardDecisionPopup(ImageView currentCard) {
        Popup.newCardDecisionPopup(currentCard.getImage()).show();

        pauseBeforeNextAnimation(500);
    }

    public static void queueGameOver(List<PlayerScores> playersScores) {
        queueUpdateStatus(LanguageManager.get("status.game_over"), 2500);
        animationQueue.offer(() -> playGameOver(playersScores));
        playNextAnimation();
    }

    private static void playGameOver(List<PlayerScores> playersScores) {
        PendingPopup.set(Popup.newFinalScoresPopup(playersScores));
        Platform.runLater(() -> SceneSwitch.switchScene("LobbyScene"));
        animationQueue.clear();
        isAnimationPlaying = false;
    }

    public static ParallelTransition getTileRemovalAnimation(double dx, double dy, Node node) {
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance == 0) distance = 1;
        double moveFactor = 100;
        double translateX = (dx / distance) * moveFactor;
        double translateY = (dy / distance) * moveFactor;

        FadeTransition fade = new FadeTransition(Duration.millis(500), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        RotateTransition rotate = new RotateTransition(Duration.millis(500), node);
        rotate.setByAngle(180);

        TranslateTransition translate = new TranslateTransition(Duration.millis(500), node);
        translate.setByX(translateX);
        translate.setByY(translateY);

        return new ParallelTransition(fade, rotate, translate);
    }

    public static ImageView getFaceDownCard(Integer cardLevel) {
        String path = "/textures/cards/card_level_" + cardLevel + "_back.png";
        Image cardImage = new Image(Objects.requireNonNull(FlightAnim.class.getResourceAsStream(path)));
        ImageView card = new ImageView(cardImage);
        card.setFitWidth(166);
        card.setFitHeight(254);
        card.setPreserveRatio(true);
        card.setRotate(Math.random() * 10 - 5);
        card.setTranslateX(Math.random() * 10 - 5);
        card.setTranslateY(Math.random() * 10 - 5);
        setShadowToCard(card);
        card.setCache(true);
        return card;
    }

    public static void generatePlayerPawns(Set<String> playerNames, StackPane gameBoardPane) {
        for (String name : playerNames) {
            StackPane pawn = ClientSession.getPlayerProfilePicture(name, 35);
            playerPawns.put(name, pawn);
            gameBoardPane.getChildren().add(pawn);
        }
    }

    public static void generateGameBoardSlots(BoardType boardType) {
        boardSlotCoordinates.clear();

        try (InputStream is = FlightAnim.class.getResourceAsStream("/public/slot_positions.json")) {
            if (is == null) throw new IOException("JSON not found!");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(is);

            String key = boardType.toString();
            JsonNode boardNode = root.get(key);

            for (Iterator<String> it = boardNode.fieldNames(); it.hasNext(); ) {
                String index = it.next();
                JsonNode pos = boardNode.get(index);
                int x = pos.get("x").asInt();
                int y = pos.get("y").asInt();
                boardSlotCoordinates.put(Integer.parseInt(index), new Point(x, y));
            }

        } catch (IOException e) {
            Logger.logError("Error reading json:" + e.getMessage());
        }
    }

    public static void queueCrewDecisionPopup(Set<ShipBoard.CrewData> crewData) {
        animationQueue.offer(() -> {
            GridPane crewGridP1 = controller.getCrewGridP1();

            ShipHandler.updateCrewPane(crewGridP1, crewData, controller.getGridCellsSize(crewGridP1));
            crewGridP1.setMouseTransparent(false);
            showPopup(Popup.newCrewDecisionPopup(controller.getShipboardP1()));
        });
        playNextAnimation();
    }
    public static void queueBatteryDecisionPopup(Set<ShipBoard.BatteriesData> batteries, Point hitPoint) {
        animationQueue.offer(() -> {
            GridPane batteriesGridP1 = controller.getBatteriesGridP1();
            double size = controller.getGridCellsSize(batteriesGridP1);

            ShipHandler.updateBatteriesPane(batteriesGridP1, batteries, size);
            batteriesGridP1.setMouseTransparent(false);
            ImageView dangerImage = getDangerImage(size);
            batteriesGridP1.add(dangerImage, hitPoint.x, hitPoint.y);
            showPopup(Popup.newBatteryDecisionPopup(controller.getShipboardP1()));
        });
        playNextAnimation();
    }

    public static void queueTileSelectionPopup(String tileType, double minPower, Set<ShipBoard.BatteriesData> batteries, Set<Point> verticalTiles, Set<Point> rotatedTiles, boolean isOpenSpace, boolean hasAlien) {
        animationQueue.offer(() -> showPopup(Popup.newTileSelectionPopup(controller.getShipboardP1(), tileType, minPower, batteries, verticalTiles, rotatedTiles, isOpenSpace, hasAlien)));
        playNextAnimation();
    }

    private static ImageView getDangerImage(double size) {
        Image image = new Image(Objects.requireNonNull(FlightAnim.class.getResourceAsStream("/textures/sprites/overlays/danger.png")));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        return imageView;
    }

    public static void queueCargoDecisionPopup(StackPane shipboard, VBox blockPool) {
        animationQueue.offer(() -> showPopup(Popup.newCargoDecisionPopup(shipboard, blockPool)));
        playNextAnimation();
    }

    private static void showPopup(Popup popup) {
        closeCurrentPopup();
        currentPopup = popup;
        currentPopup.show();
    }

    public static void closeCurrentPopup() {
        if(currentPopup == null) return;
        controller.getCrewGridP1().setMouseTransparent(true);
        controller.getBatteriesGridP1().setMouseTransparent(true);
        controller.getCargoGridP1().setMouseTransparent(true);
        controller.getP1Box().getChildren().add(controller.getShipboardP1());
        currentPopup.playClosingAnimation();
        currentPopup = null;
        startNextAnimation();
    }
}
