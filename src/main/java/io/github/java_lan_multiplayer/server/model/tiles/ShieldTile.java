package io.github.java_lan_multiplayer.server.model.tiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a shield tile which protects the ship from damage.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class ShieldTile extends Tile {

    /**
     * Constructs a ShieldTile.
     *
     * @param id         unique tile identifier
     * @param connectors array of connectors on the tile sides
     */
    public ShieldTile(@JsonProperty("id") int id, @JsonProperty("connectors") Connector[] connectors) {
        super(id, connectors);
    }

    @Override
    public String getTileType() {
        return "Shield";
    }
}
