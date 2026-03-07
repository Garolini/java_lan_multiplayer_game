package io.github.java_lan_multiplayer.common.messages.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;
import io.github.java_lan_multiplayer.server.model.ShipBoard;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CrewMessage implements GameMessage {

    private String type;

    private String playerName;
    private Set<ShipBoard.CrewData> crew;

    public CrewMessage() {}

    public CrewMessage(String type, String playerName, Set<ShipBoard.CrewData> crew) {
        this.type = type;
        this.playerName = playerName;
        this.crew = crew;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Set<ShipBoard.CrewData> getCrew() {
        return crew;
    }

    @Override
    public String getType() {
        return type;
    }
}
