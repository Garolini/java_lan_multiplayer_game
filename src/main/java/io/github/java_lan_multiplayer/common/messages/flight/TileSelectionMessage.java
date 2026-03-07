package io.github.java_lan_multiplayer.common.messages.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;
import io.github.java_lan_multiplayer.server.model.ShipBoard;

import java.awt.*;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TileSelectionMessage implements GameMessage {

    private String type;

    private String playerName;

    private double minPower;
    private Set<Point> verticalTiles;
    private Set<Point> rotatedTiles;
    private Set<ShipBoard.BatteriesData> batteries;
    private boolean openSpace;
    private boolean hasAlien;

    public TileSelectionMessage() {}

    public TileSelectionMessage(String type, String playerName, double minPower, Set<Point> verticalTiles, Set<Point> rotatedTiles, Set<ShipBoard.BatteriesData> batteries, boolean openSpace, boolean hasAlien) {
        this.type = type;
        this.playerName = playerName;
        this.minPower = minPower;
        this.verticalTiles = verticalTiles;
        this.rotatedTiles = rotatedTiles;
        this.batteries = batteries;
        this.openSpace = openSpace;
        this.hasAlien = hasAlien;
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getMinPower() {
        return minPower;
    }

    public Set<Point> getVerticalTiles() {
        return verticalTiles;
    }
    public Set<Point> getRotatedTiles() {
        return rotatedTiles;
    }
    public boolean getOpenSpace() {
        return openSpace;
    }
    public boolean getHasAlien() {
        return hasAlien;
    }

    public Set<ShipBoard.BatteriesData> getBatteries() {
        return batteries;
    }

    @Override
    public String getType() {
        return type;
    }
}
