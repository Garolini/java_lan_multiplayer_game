package io.github.java_lan_multiplayer.common.messages.shipBuilding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DoneBuildingMessage implements GameMessage {

    private String playerName;
    private int positionIndex;

    public DoneBuildingMessage() {}

    public DoneBuildingMessage(String playerName, int positionIndex) {
        this.playerName = playerName;
        this.positionIndex = positionIndex;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    @Override
    public String getType() {
        return "done_building";
    }
}
