package io.github.java_lan_multiplayer.server.model.tiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import static io.github.java_lan_multiplayer.server.model.tiles.CrewType.*;

/**
 * Represents a cabin tile that can host human crew or specific types of aliens.
 * <p>
 * By default, cabins contain a {@link CrewType#DOUBLE_HUMAN} crew. Depending on the configuration,
 * they may be able to host alien crew types (Brown or Purple aliens). The tile tracks both
 * its current crew and its ability to host each alien type.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class CabinTile extends Tile {

    private boolean canHostBrownAlien = false;
    private boolean canHostPurpleAlien = false;
    private CrewType crew;

    /**
     * Creates a CabinTile with the given ID and connectors. Initially hosts a {@code DOUBLE_HUMAN} crew.
     *
     * @param id         the unique identifier for this tile
     * @param connectors the tile's connectors (in NESW order)
     */
    public CabinTile(@JsonProperty("id") int id, @JsonProperty("connectors") Connector[] connectors) {
        super(id, connectors);
        this.crew = DOUBLE_HUMAN;
    }

    /**
     * Returns the current crew in the cabin.
     */
    public CrewType getCrew() {
        return crew;
    }

    /**
     * Sets the type of crew currently occupying the cabin. Throws if trying to assign an alien type
     * not allowed by the cabin's configuration.
     *
     * @param type the crew type to assign
     * @throws IllegalArgumentException if the alien type is not allowed
     */
    public void setCrewType(CrewType type) {
        if (type.isAlien()) {
            if (type == CrewType.BROWN_ALIEN && !canHostBrownAlien) {
                throw new IllegalArgumentException("Can't host brown alien");
            }
            if (type == CrewType.PURPLE_ALIEN && !canHostPurpleAlien) {
                throw new IllegalArgumentException("Can't host purple alien");
            }
        }
        this.crew = type;
    }

    /**
     * Updates this cabin's ability to host a specific alien type.
     * If the cabin already contains an alien of that type, and it's disallowed, the crew is removed.
     *
     * @param alienType       the alien type
     * @param alienHabitable  whether this cabin can now host that alien type
     * @throws IllegalArgumentException if the given type is not an alien
     */
    public void allowAlienType(CrewType alienType, boolean alienHabitable) {
        if(!alienType.isAlien()) throw new IllegalArgumentException(alienType + " is not an alien type.");
        if(alienType == BROWN_ALIEN) canHostBrownAlien = alienHabitable;
        else if(alienType == PURPLE_ALIEN) canHostPurpleAlien = alienHabitable;
        if(!alienHabitable && hasAlien(alienType)) crew = NONE;
    }
    /**
     * Returns whether the cabin currently contains an alien of the given type.
     *
     * @param alienType the alien type to check
     * @return {@code true} if the alien is currently in the cabin
     */
    public boolean hasAlien(CrewType alienType) {
        if(!alienType.isAlien()) throw new IllegalArgumentException(alienType + " is not an alien type.");
        return crew == alienType;
    }
    /**
     * Returns whether this cabin can host the specified alien type.
     *
     * @param alienType the alien type
     * @return {@code true} if this cabin can host it
     */
    public boolean canHostAlienType(CrewType alienType) {
        if(!alienType.isAlien()) throw new IllegalArgumentException(alienType + " is not an alien type.");
        return (alienType == BROWN_ALIEN)? canHostBrownAlien : canHostPurpleAlien;
    }
    /**
     * Returns whether this cabin can host at least one alien type.
     */
    public boolean canHostAnAlienType() {
        return canHostAlienType(CrewType.BROWN_ALIEN) || canHostAlienType(CrewType.PURPLE_ALIEN);
    }

    /**
     * Returns whether the cabin currently has any crew.
     */
    public boolean hasCrew() {
        return crew != CrewType.NONE;
    }

    /**
     * Returns the number of human crew members in the cabin.
     */
    public int getHumanCount() {
        return crew.humanCount();
    }

    /**
     * Removes one member from the cabin's current crew.
     * If {@code DOUBLE_HUMAN}, becomes {@code SINGLE_HUMAN}.
     * Otherwise, becomes empty.
     *
     * @return the type of crew that was removed
     */
    public CrewType removeMember() {
        CrewType removed = crew;

        if(crew == DOUBLE_HUMAN) crew = SINGLE_HUMAN;
        else crew = NONE;

        return removed;
    }

    /**
     * Returns a list of valid {@link CrewType} options the player can cycle through,
     * based on what this cabin is allowed to host.
     */
    public List<CrewType> getValidCrewTypesForCycle() {
        List<CrewType> options = new ArrayList<>();
        options.add(CrewType.DOUBLE_HUMAN);
        if (canHostAlienType(CrewType.BROWN_ALIEN)) options.add(CrewType.BROWN_ALIEN);
        if (canHostAlienType(CrewType.PURPLE_ALIEN)) options.add(CrewType.PURPLE_ALIEN);
        return options;
    }

    @Override
    public String getTileType() {
        return "Cabin";
    }
}