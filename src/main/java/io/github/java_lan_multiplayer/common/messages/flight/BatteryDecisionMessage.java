package io.github.java_lan_multiplayer.common.messages.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;
import io.github.java_lan_multiplayer.server.model.ShipBoard;

import java.awt.*;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BatteryDecisionMessage implements GameMessage {

    private String playerName;
    private Set<ShipBoard.BatteriesData> batteries;
    private Point hitPoint;

    public BatteryDecisionMessage() {}

    public BatteryDecisionMessage(String playerName, Set<ShipBoard.BatteriesData> batteries, Point hitPoint) {
        this.playerName = playerName;
        this.batteries = batteries;
        this.hitPoint = hitPoint;
    }

    public String getPlayerName() {
        return playerName;
    }
    public Set<ShipBoard.BatteriesData> getBatteries() {
        return batteries;
    }
    public Point getHitPoint() {
        return hitPoint;
    }

    @Override
    public String getType() {
        return "battery_decision";
    }
}
