package io.github.java_lan_multiplayer.server.model.tiles;

/**
 * Represents the starting cabin tile in the game.
 * This tile always has universal connectors and must contain only human crew.
 * Alien crew members are explicitly not allowed.
 */
public class StartingCabinTile extends CabinTile {

    /**
     * Constructs a StartingCabinTile with universal connectors and ID -1.
     */
    public StartingCabinTile() {
        super(-1, new Connector[]{Connector.UNIVERSAL,Connector.UNIVERSAL,Connector.UNIVERSAL,Connector.UNIVERSAL});
    }

    /**
     * Overrides alien support to disallow any alien habitation.
     *
     * @param alienType       the type of alien
     * @param alienHabitable  true if the tile should support the alien type
     * @throws IllegalArgumentException always if alienHabitable is true
     */
    @Override
    public void allowAlienType(CrewType alienType, boolean alienHabitable) {
        if(alienHabitable) throw new IllegalArgumentException("Starting cabin cannot host aliens.");
    }

    /**
     * Ensures only human crew types can be set in the starting cabin.
     *
     * @param crewType the crew type to assign
     * @throws IllegalArgumentException if the crew type is an alien
     */
    @Override
    public void setCrewType(CrewType crewType) {
        if(!crewType.isHuman()) throw new IllegalArgumentException("Starting cabin cannot host aliens.");
        super.setCrewType(crewType);
    }

    @Override
    public String getTileType() {
        return "Starting Cabin";
    }
}