package io.github.java_lan_multiplayer.common.messages.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;
import io.github.java_lan_multiplayer.server.model.ShipBoard;
import io.github.java_lan_multiplayer.server.model.tiles.BlockType;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CargoDecisionMessage implements GameMessage {

    private String playerName;
    private Set<ShipBoard.CargoData> cargoTiles;
    private List<BlockType> cargoPool;

    public CargoDecisionMessage() {}

    public CargoDecisionMessage(String playerName, Set<ShipBoard.CargoData> cargoTiles, List<BlockType> cargoPool) {
        this.playerName = playerName;
        this.cargoTiles = cargoTiles;
        this.cargoPool = cargoPool;
    }

    public String getPlayerName() {
        return playerName;
    }
    public Set<ShipBoard.CargoData> getCargoTiles() {
        return cargoTiles;
    }
    public List<BlockType> getCargoPool() {
        return cargoPool;
    }

    @Override
    public String getType() {
        return "cargo_decision";
    }
}
