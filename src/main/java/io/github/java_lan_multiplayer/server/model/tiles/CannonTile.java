package io.github.java_lan_multiplayer.server.model.tiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a cannon tile, which can either be a standard cannon or a double cannon.
 * <p>
 * Cannon strength depends on its rotation and whether it is a double cannon:
 * <ul>
 *     <li>Single cannon facing forward: strength = 1</li>
 *     <li>Single cannon sideways: strength = 0.5</li>
 *     <li>Double cannon facing forward (rotation 0): strength = 2</li>
 *     <li>Double cannon sideways (rotation != 0): strength = 1</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class CannonTile extends ActivatableTile {

    /**
     * Constructs a new CannonTile with the given ID, connectors, and cannon type.
     *
     * @param id         unique tile ID
     * @param connectors connector configuration (NESW)
     * @param isDouble   whether this is a double cannon
     */
    public CannonTile(@JsonProperty("id") int id, @JsonProperty("connectors") Connector[] connectors, @JsonProperty("isDouble") boolean isDouble) {
        super(id, connectors, isDouble);
    }

    /**
     * Calculates the firing strength of the cannon based on its rotation and type.
     * <p>
     * Rotation is clockwise in 90-degree increments: 0 = forward, 1 = right, etc.
     *
     * @param rotation the current rotation (0–3)
     * @param isDouble whether the cannon is a double cannon
     * @return the firing strength (2, 1, or 0.5)
     */
    public static double getStrength(int rotation, boolean isDouble) {
        if(isDouble) return (rotation == 0) ? 2 : 1;
        return (rotation == 0) ? 1 : 0.5;
    }

    @Override
    public String getTileType() {
        return isDouble()? "Double Cannon" : "Cannon";
    }
}
