package io.github.java_lan_multiplayer.server.model.tiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a structural tile, used for connecting other modules.
 * Structural tiles typically have no functionality on their own.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class StructuralTile extends Tile {

    /**
     * Constructs a StructuralTile.
     *
     * @param id         unique tile identifier
     * @param connectors array of connectors on the tile sides
     */
    public StructuralTile(@JsonProperty("id") int id, @JsonProperty("connectors") Connector[] connectors) {
        super(id, connectors);
    }

    @Override
    public String getTileType() {
        return "Structural Module";
    }
}
