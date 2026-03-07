package io.github.java_lan_multiplayer.server.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.events.flight.cards.CargoDecisionEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.PlanetSelectionEvent;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.tiles.BlockType;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.java_lan_multiplayer.server.model.cards.Card.InputState.*;

/**
 * Represents a "Planets" card where players can choose to land on planets and collect cargo.
 * After choosing, players move backward and perform cargo operations like loading and unloading.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class PlanetsCard extends Card implements CargoHandlerCard {

    private final BlockType[][] planets;
    private final int positionCost;

    private List<Player> playerQueue;

    private Player[] planetChosenBy;

    public PlanetsCard(
            @JsonProperty("id") int id,
            @JsonProperty("level") int level,
            @JsonProperty("planets") BlockType[][] planets,
            @JsonProperty("positionCost") int positionCost) {
        super(id, level);
        if (planets.length < 1 || planets.length > 4) throw new IllegalArgumentException("There must be between 1 and 4 planets.");
        this.planets = planets;
        if (positionCost < 0) throw new IllegalArgumentException("positionCost must be a positive number.");
        this.positionCost = positionCost;
    }

    /**
     * Activates the card and begins the planet selection process.
     * Players are prompted in turn to choose a planet or refuse.
     */
    @Override
    protected void onActivate() {
        this.playerQueue = board.getSortedPlayers();
        this.planetChosenBy = new Player[planets.length];
        setInputState(PLANET_SELECTION);

        dispatcher.fireEvent(new PlanetSelectionEvent(playerQueue.getFirst().getName(), planetChosenBy));
    }

    /**
     * Called when a player chooses a planet to land on.
     * Assigns cargo from that planet and moves to the next player's turn.
     *
     * @param caller the player choosing the planet
     * @param index  the index of the chosen planet
     * @throws IllegalArgumentException if index is out of bounds
     */
    public void choosePlanet(Player caller, int index) {
        verifyInputState(PLANET_SELECTION);
        verifyPlayerTurn(caller, playerQueue);
        if(index < 0 || index >= planets.length) throw new IllegalArgumentException("Planet index out of bounds.");
        if (planetChosenBy[index] != null) {
            Logger.logWarning("Planet " + index + " is already taken.");
            return;
        }
        planetChosenBy[index] = caller;
        caller.setCargoPool(new ArrayList<>(Arrays.asList(planets[index])));
        playerQueue.removeFirst();

        if(!playerQueue.isEmpty()) {
            dispatcher.fireEvent(new PlanetSelectionEvent(playerQueue.getFirst().getName(), planetChosenBy));
        } else {
            setInputState(NONE);
            giveRewards();
        }
    }

    /**
     * Called when a player refuses to land on any planet.
     * Skips their turn and proceeds to the next player.
     *
     * @param caller the player refusing to land
     */
    public void refusePlanet(Player caller) {
        verifyInputState(PLANET_SELECTION);
        verifyPlayerTurn(caller, playerQueue);
        playerQueue.removeFirst();

        if(!playerQueue.isEmpty()) {
            dispatcher.fireEvent(new PlanetSelectionEvent(playerQueue.getFirst().getName(), planetChosenBy));
        } else {
            setInputState(NONE);
            giveRewards();
        }
    }

    private void giveRewards() {
        verifyInputState(NONE);

        boolean anyAccepted = Arrays.stream(planetChosenBy).anyMatch(Objects::nonNull);
        if (!anyAccepted) {
            setInputState(FINISHED);
            return;
        }
        for (Player player : board.getSortedPlayers().reversed()) {
            if (Arrays.asList(planetChosenBy).contains(player)) {
                board.movePlayerBackward(player, positionCost);
            }
        }
        setInputState(CARGO_DECISION);
        Set<Player> accepted = Arrays.stream(planetChosenBy).filter(Objects::nonNull).collect(Collectors.toSet());
        dispatcher.fireEvent(new CargoDecisionEvent(accepted));
    }

    /**
     * Called when a player chooses to load cargo from a chosen planet to a ship container.
     *
     * @param caller         the player performing the cargo operation
     * @param blockIndex     the index of the cargo block being loaded
     * @param tileCoords     the tile where the cargo should be placed
     * @param containerIndex the index of the container on the ship
     */
    @Override
    public void loadCargo(Player caller, int blockIndex, Point tileCoords, int containerIndex) {
        verifyInputState(CARGO_DECISION);
        verifyPlayerInList(caller, Arrays.asList(planetChosenBy), "Player " + caller.getName() + " has no active cargo session.");

        caller.swapCargo(blockIndex, tileCoords, containerIndex);
    }

    /**
     * Called when a player chooses to unload cargo from a ship container to a tile.
     *
     * @param caller         the player performing the unload operation
     * @param tileCoords     the tile where the cargo should be unloaded
     * @param containerIndex the index of the container being unloaded
     */
    @Override
    public void unloadCargo(Player caller, Point tileCoords, int containerIndex) {
        verifyInputState(CARGO_DECISION);
        verifyPlayerInList(caller, Arrays.asList(planetChosenBy), "Player " + caller.getName() + " has no active cargo session.");

        caller.unloadCargo(tileCoords, containerIndex);
    }

    /**
     * Called when a player finishes their cargo operations.
     * Removes the player from the cargo session and ends the phase if no players remain.
     *
     * @param caller the player finishing their turn
     */
    @Override
    public void confirmDone(Player caller) {
        verifyInputState(CARGO_DECISION);
        verifyPlayerInList(caller, Arrays.asList(planetChosenBy), "Player " + caller.getName() + " has no active cargo session.");

        for (int i = 0; i < planetChosenBy.length; i++) {
            if (planetChosenBy[i] == caller) {
                planetChosenBy[i] = null;
            }
        }
        caller.clearCargoPool();

        boolean anyRemaining = Arrays.stream(planetChosenBy).anyMatch(Objects::nonNull);
        if (!anyRemaining) {
            setInputState(FINISHED);
        }
    }

    @Override
    public String getCardType() {
        return "Planets";
    }
}
