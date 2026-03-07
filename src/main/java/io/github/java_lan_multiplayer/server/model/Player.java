package io.github.java_lan_multiplayer.server.model;

import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.flight.TileInfo;
import io.github.java_lan_multiplayer.server.events.EventDispatcher;
import io.github.java_lan_multiplayer.server.events.flight.*;
import io.github.java_lan_multiplayer.server.events.flight.cards.CargoSwappedEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.CargoUnloadedEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.DecisionEndEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.ProjectileEvent;
import io.github.java_lan_multiplayer.server.model.tiles.*;
import io.github.java_lan_multiplayer.server.model.cards.projectile.Projectile;

import java.awt.*;
import java.util.*;
import java.util.List;

import static io.github.java_lan_multiplayer.server.model.Player.PlayerState.*;

/**
 * Represents a player in the game, managing their state, ship, cargo, and interactions.
 * Handles state changes, cargo operations, tile reservations, and event dispatching.
 */
public class Player {

    public enum Color {
        BLUE, RED, GREEN, YELLOW
    }
    public enum PlayerState {
        IDLE,
        READY_TO_PLAY,
        BUILDING_SHIP,
        DONE_BUILDING,
        GIVING_UP,
        ELIMINATED
    }

    private final String username;
    private final int[] profilePictureIds;
    private Color color;
    private ShipBoard shipBoard;
    private int credits;
    private int position;

    private PlayerState playerState = IDLE;
    private final Integer[] reservedTiles;
    private boolean admin;

    private List<BlockType> cargoPool;

    protected EventDispatcher dispatcher = new EventDispatcher();

    /**
     * Constructs a Player with a given username and profile picture IDs.
     * ShipBoard and other properties are initialized with default values.
     *
     * @param username the player's name
     * @param profilePictureIds an array of profile picture IDs
     */
    public Player(String username, int[] profilePictureIds) {
        this.username = username;
        this.profilePictureIds = profilePictureIds;
        this.color = null;
        this.shipBoard = null;
        this.credits = 0;
        this.position = 0;
        this.reservedTiles = new Integer[2];
        Arrays.fill(reservedTiles, null);
        this.admin = false;
    }
    /**
     * Constructs a Player with a pre-assigned ShipBoard, used primarily for testing.
     *
     * @param username the player's name
     * @param profilePictureIds an array of profile picture IDs
     * @param shipBoard the ship board to assign to the player
     */
    public Player(String username, int[] profilePictureIds, ShipBoard shipBoard) {
        this(username, profilePictureIds);
        this.shipBoard = shipBoard;
    }

    // GETTERS AND SETTERS
    public String getName() {
        return username;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public Color getColor() {
        return color;
    }
    public int[] getProfilePictureIds() {
        return profilePictureIds;
    }
    public void setNewShipBoard(BoardType shipType) {
        this.shipBoard = new ShipBoard(shipType);
    }
    public ShipBoard getShipBoard() {
        return shipBoard;
    }
    public void giveCredits(int credits) {
        this.credits += credits;
        dispatcher.fireEvent(new CreditsUpdateEvent(username, credits));
    }
    public int getCredits() {
        return credits;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public int getPosition() {
        return position;
    }
    public PlayerState getState() {
        return playerState;
    }
    public void setState(PlayerState state) {
        this.playerState = state;
        if(state == ELIMINATED) {
            dispatcher.fireEvent(new PlayerEliminatedEvent(username));
        }
    }
    public List<BlockType> getCargoPool() {
        return cargoPool;
    }
    public void setCargoPool(List<BlockType> cargoPool) {
        this.cargoPool = cargoPool;
    }
    public void assignEventDispatcher(EventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
    public boolean isAdmin() {
        return admin;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    // DELEGATE METHODS

    public void setTile(int x, int y, int tileId, int rotation) {
        shipBoard.setTile(x, y, tileId, rotation);
    }
    public void removeTile(int x, int y) {
        shipBoard.removeTile(x, y);
        dispatcher.fireEvent(new ShipUpdateEvent(username,getSimplifiedShipBoard()));
    }
    public int getTotalTileCount() {
        return shipBoard.getTotalTileCount();
    }
    public int getCrewCount() {
        return shipBoard.getTotalCrewCount();
    }
    public int getHumanCount() {
        return shipBoard.getTotalHumanCount();
    }
    public int getExposedConnectorsCount() {
        return shipBoard.getExposedConnectorsCount();
    }
    public void applyEpidemicCard() {
        shipBoard.applyEpidemicCard();
        dispatcher.fireEvent(new CrewUpdateEvent(username, getShipContent().crewData()));
    }
    public void removeMostValuableCargo(int quantity) {
        shipBoard.removeMostValuableCargo(quantity);
        dispatcher.fireEvent(new CargoUpdateEvent(username, getShipContent().cargoData()));
        dispatcher.fireEvent(new BatteriesUpdateEvent(username, getShipContent().batteriesData()));
    }
    public double getMaxStrength(Class<? extends ActivatableTile> tileClass) {
        return shipBoard.getMaxStrength(tileClass);
    }
    public double getMinStrength(Class<? extends ActivatableTile> tileClass) {
        return shipBoard.getMinStrength(tileClass);
    }
    public double getStrength(Class<? extends ActivatableTile> tileClass, int doubleVertical, int doubleRotated) {
        return shipBoard.getStrength(tileClass, doubleVertical, doubleRotated);
    }
    public double getStrength(Class<? extends ActivatableTile> tileClass, int doubleVertical) {
        return shipBoard.getStrength(tileClass, doubleVertical, 0);
    }
    public void removeBatteries(List<Point> coordinates) {
        shipBoard.removeBatteries(coordinates);
        dispatcher.fireEvent(new BatteriesUpdateEvent(username, getShipContent().batteriesData()));
    }
    public void applyProjectile(Projectile projectile) {
        Point hitPoint = shipBoard.removeFirstTileInPath(projectile.getSource(), projectile.getPathIndex());

        dispatcher.fireEvent(new ProjectileEvent(this, projectile, hitPoint, false));

        if(hitPoint != null) {
            dispatcher.fireEvent(new CrewUpdateEvent(username, getShipContent().crewData()));
            dispatcher.fireEvent(new CargoUpdateEvent(username, getShipContent().cargoData()));
            dispatcher.fireEvent(new BatteriesUpdateEvent(username, getShipContent().batteriesData()));
        }
    }
    public ShipBoard.DefendingCannonType getDefendingCannonType(Projectile projectile) {
        ShipBoard.DefendingCannonType cannonType = shipBoard.getDefendingCannonType(projectile.getSource(), projectile.getPathIndex());
        if(cannonType == ShipBoard.DefendingCannonType.SINGLE) {
            dispatcher.fireEvent(new ProjectileEvent(this, projectile, null, true));
        }
        return cannonType;
    }
    public boolean isPathEmpty(Projectile projectile) {
        if(shipBoard.isPathEmpty(projectile.getSource(), projectile.getPathIndex())) {
            dispatcher.fireEvent(new ProjectileEvent(this, projectile, null, false));
            return true;
        }
        return false;
    }
    public boolean isSideShielded(int side) {
        return shipBoard.isSideShielded(side);
    }
    public boolean hasExposedConnectorInPath(Projectile projectile) {
        if(shipBoard.hasExposedConnectorInPath(projectile.getSource(), projectile.getPathIndex())) return true;
        Point tilePoint = getFirstNonEmptyTilePosition(projectile.getSource(), projectile.getPathIndex());
        dispatcher.fireEvent(new ProjectileEvent(this, projectile, tilePoint, true));
        return false;
    }
    public int getCargoHoldsValue() {
        return shipBoard.getTotalHoldsValue();
    }
    public void updateConnectivity() {
        shipBoard.updateConnectivity();
    }
    public Set<Point> getInvalidTiles() {
        return shipBoard.getInvalidTiles();
    }
    public ShipBoard.ShipContent getShipContent() {
        return shipBoard.getShipContent();
    }
    public Set<Point> getCabinTilesThatCanHostAliens() {
        return shipBoard.getCabinTilesThatCanHostAliens();
    }
    public void cycleCrewTypeAt(int x, int y) {
        shipBoard.cycleCrewTypeAt(x, y);
        dispatcher.fireEvent(new CrewUpdateEvent(username, getShipContent().crewData()));
    }
    public Point getFirstNonEmptyTilePosition(Projectile.Source source, int index) {
        return shipBoard.getFirstNonEmptyTilePosition(source, index);
    }
    public int getRemovedTilesCount() {
        return shipBoard.getRemovedTilesCount();
    }
    public int getTotalHoldsValue() {
        return shipBoard.getTotalHoldsValue();
    }
    public Set<Point> getDoubleCannonsPositions(boolean vertical) {
        return shipBoard.getDoubleCannonsPositions(vertical);
    }
    public Set<Point> getDoubleEnginesPositions() {
        return shipBoard.getDoubleEnginesPositions();
    }
    public boolean hasAlien(CrewType alienType) {
        return shipBoard.hasAlien(alienType);
    }

    // NEW METHODS

    public boolean isReady() {
        return playerState == READY_TO_PLAY;
    }
    /**
     * Toggles the player's state between IDLE and READY_TO_PLAY.
     * Throws an exception if the current state is not toggleable.
     *
     * @throws IllegalStateException if state is not IDLE or READY_TO_PLAY
     */
    public void toggleReady() {
        switch (playerState) {
            case IDLE -> setState(READY_TO_PLAY);
            case READY_TO_PLAY -> setState(IDLE);
            default -> throw new IllegalStateException("Cannot toggle ready state from " + playerState);
        }
    }

    /**
     * Reserves a tile at the given index (0 or 1).
     *
     * @param tileId the ID of the tile to reserve
     * @param index the index of the reserved slot (0 or 1)
     * @throws IllegalArgumentException if the index is invalid or slot is occupied
     */
    public void reserveTile(int tileId, int index) {
        if(index < 0 || index > 1) throw new IllegalArgumentException("Invalid index: " + index + ".");
        if(reservedTiles[index] != null) throw new IllegalArgumentException("Slot occupied.");
        reservedTiles[index] = tileId;
    }
    /**
     * Uses a previously reserved tile and clears the reserved slot.
     *
     * @param index the index of the reserved slot (0 or 1)
     * @return the ID of the reserved tile
     * @throws IllegalArgumentException if index is invalid or slot is empty
     */
    public int useReservedTile(int index) {
        if(index < 0 || index > 1) throw new IllegalArgumentException("Invalid index: " + index + ".");
        if(reservedTiles[index] == null) throw new IllegalArgumentException("Slot is empty.");
        int tile = reservedTiles[index];
        reservedTiles[index] = null;
        return tile;
    }
    private int getReservedTileCount() {
        int count = 0;
        for (Integer reservedTile : reservedTiles) {
            if (reservedTile != null) count++;
        }
        return count;
    }
    /**
     * Applies a penalty for holding onto reserved tiles by increasing the removed tile count.
     * Clears all reserved tiles.
     */
    public void applyReservedTilePenalty() {
        shipBoard.addToRemovedTiles(getReservedTileCount());
        Arrays.fill(reservedTiles, null);
    }

    /**
     * Ensures a crew member is removed from a specified cabin tile.
     *
     * @param cabinCoords coordinates of the cabin tile
     * @throws IllegalArgumentException if no crew member is present at the specified tile
     */
    public void ensureCrewRemovedAt(Point cabinCoords) {
        CrewType removedMember = shipBoard.removeCrewMemberAt(cabinCoords.x, cabinCoords.y);
        if(removedMember == null) throw new IllegalArgumentException("Cabin tile does not have any crew members.");

        dispatcher.fireEvent(new CrewUpdateEvent(username, getShipContent().crewData()));
    }

    /**
     * Ensures a battery is removed from a tile at the specified coordinates.
     *
     * @param batteryCoords coordinates of the battery tile
     * @throws IllegalArgumentException if the tile doesn't contain a battery
     */
    public void ensureBatteryRemovedFrom(Point batteryCoords) {
        boolean isBatteryRemoved = shipBoard.removeBatteryFrom(batteryCoords.x, batteryCoords.y);
        if(!isBatteryRemoved) throw new IllegalArgumentException("Battery tile does not have any battery.");

        dispatcher.fireEvent(new BatteriesUpdateEvent(username, getShipContent().batteriesData()));
    }

    /**
     * Swaps a block from the cargo pool into a cargo container on the ship.
     * Handles red block restrictions and potential block replacement.
     *
     * @param blockIndex the index of the block in the cargo pool
     * @param tileCoords coordinates of the cargo tile
     * @param containerIndex the index within the tile’s container
     * @throws IllegalArgumentException if the block index is invalid
     * @throws RuntimeException if cargo pool is not initialized
     */
    public void swapCargo(int blockIndex, Point tileCoords, int containerIndex) {
        if(cargoPool == null) throw new RuntimeException("cargoPool has not been initialized.");
        if(blockIndex < 0 || blockIndex >= cargoPool.size()) throw new IllegalArgumentException("Invalid block index.");

        BlockType selectedBlock = cargoPool.get(blockIndex);

        if(getShipBoard().getTile(tileCoords.x, tileCoords.y) instanceof CargoTile cargo) {
            if(cargo.getType() == CargoTile.CargoType.NORMAL && selectedBlock == BlockType.RED) {
                Logger.logDebug("Trying to place Red block inside Normal container.");
                return;
            }
        }

        BlockType replaced = shipBoard.loadCargoAt(tileCoords.x, tileCoords.y, containerIndex, selectedBlock);

        cargoPool.remove(blockIndex);
        if(replaced != null) cargoPool.add(replaced);

        dispatcher.fireEvent(new CargoSwappedEvent(this, blockIndex, tileCoords,containerIndex));
    }

    /**
     * Unloads cargo from the ship to the player's cargo pool.
     *
     * @param tileCoords coordinates of the cargo tile
     * @param containerIndex the index within the tile’s container
     * @throws RuntimeException if cargo pool is not initialized
     */
    public void unloadCargo(Point tileCoords, int containerIndex) {
        if(cargoPool == null) throw new RuntimeException("cargoPool has not been initialized.");

        BlockType removed = shipBoard.unloadCargoAt(tileCoords.x, tileCoords.y, containerIndex);

        if(removed != null) cargoPool.add(removed);

        dispatcher.fireEvent(new CargoUnloadedEvent(this, tileCoords,containerIndex));
    }

    /**
     * Clears the player's cargo pool and signals the end of a decision-making phase.
     */
    public void clearCargoPool() {
        this.cargoPool = null;

        dispatcher.fireEvent(new DecisionEndEvent(username));
    }

    /**
     * Returns a simplified representation of the player's ship board.
     * Only non-empty and are included.
     *
     * @return a set of TileInfo representing placed tiles
     */
    public Set<TileInfo> getSimplifiedShipBoard() {
        Set<TileInfo> simplifiedShipBoard = new HashSet<>();
        Tile[][] tiles = shipBoard.getTiles();
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 7; x++) {
                Tile tile = tiles[y][x];
                if (tile != null && !(tile instanceof EmptyTile)) {
                    simplifiedShipBoard.add(new TileInfo(tile.getId(), tile.getRotation(), x, y));
                }
            }
        }
        return simplifiedShipBoard;
    }
}