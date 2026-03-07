package io.github.java_lan_multiplayer.client.popups;

import io.github.java_lan_multiplayer.client.ClientSession;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.endGame.PlayerScores;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

public class FinalScoresPopupController implements PopupAware {

    private Popup popup;
    private static boolean isPopupVisible = false;

    @FXML private GridPane scoresGrid;

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

        if (args[0] instanceof List<?> list && !list.isEmpty() && list.getFirst() instanceof PlayerScores) {
            List<PlayerScores> scores = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof PlayerScores ps) {
                    scores.add(ps);
                } else {
                    Logger.logWarning("List contains invalid element: " + obj);
                }
            }
            listScores(scores);
        }
    }

    private void listScores(List<PlayerScores> scores) {

        int rowIndex = 1;

        for (PlayerScores player : scores) {
            RowConstraints separatorConstraints = new RowConstraints();
            separatorConstraints.setMinHeight(Region.USE_PREF_SIZE);
            separatorConstraints.setPrefHeight(10);
            separatorConstraints.setMaxHeight(Region.USE_PREF_SIZE);

            Line separator = new Line(0, 0, 1, 0);
            separator.endXProperty().bind(scoresGrid.widthProperty());

            scoresGrid.add(separator, 0, rowIndex++, 7, 1);
            scoresGrid.getRowConstraints().add(separatorConstraints);

            RowConstraints dataConstraints = new RowConstraints();
            dataConstraints.setMinHeight(Region.USE_PREF_SIZE);
            dataConstraints.setPrefHeight(60);
            dataConstraints.setMaxHeight(Region.USE_PREF_SIZE);

            scoresGrid.add(ClientSession.getPlayerProfilePicture(player.getPlayerName(), 50), 0, rowIndex);
            Label playerNameLabel = new Label(player.getPlayerName());
            playerNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18;");
            scoresGrid.add(playerNameLabel, 1, rowIndex);
            scoresGrid.add(createScoreLabel(player.getPositionScore()), 2, rowIndex);
            scoresGrid.add(createScoreLabel(player.getBestShipScore()), 3, rowIndex);
            scoresGrid.add(createScoreLabel(player.getCargoScore()), 4, rowIndex);
            scoresGrid.add(createScoreLabel(player.getLostTilesScore()), 5, rowIndex);
            Label finalScoreLabel = new Label(String.valueOf(player.getFinalScore()));
            finalScoreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 24");
            scoresGrid.add(finalScoreLabel, 6, rowIndex);

            scoresGrid.getRowConstraints().add(dataConstraints);
            rowIndex++;
        }
    }

    private Label createScoreLabel(int score) {
        Label label = new Label(String.valueOf(score));
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 18;");
        return label;
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
