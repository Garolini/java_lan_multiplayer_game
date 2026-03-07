package io.github.java_lan_multiplayer.server.model.tiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an engine tile, which contributes propulsion to the ship.
 * Engine tiles must be facing backward (rotation = 0) to be valid.
 * Can be either a single or double engine.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class EngineTile extends ActivatableTile {

    /**
     * Constructs an EngineTile.
     *
     * @param id         unique tile identifier
     * @param connectors array of connectors on the tile sides
     * @param isDouble   true if the engine is a double engine
     */
    public EngineTile(@JsonProperty("id") int id, @JsonProperty("connectors") Connector[] connectors, @JsonProperty("isDouble") boolean isDouble) {
        super(id, connectors, isDouble);
    }

    /**
     * Returns the strength provided by the engine.
     * Must only be called with rotation = 0.
     *
     * @param rotation  must be 0 (engines can't rotate)
     * @param isDouble  true if the engine is double
     * @return engine strength as double
     * @throws IllegalArgumentException if rotation is not 0
     */
    public static double getStrength(int rotation, boolean isDouble) {
        if(rotation != 0) throw new IllegalArgumentException("Engine tiles cannot be rotated");
        return isDouble? 2 : 1;
    }

    @Override
    public String getTileType() {
        return isDouble()? "Double Engine" : "Engine";
    }
}
