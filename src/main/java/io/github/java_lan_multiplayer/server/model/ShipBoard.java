package io.github.java_lan_multiplayer.server.model;

import io.github.java_lan_multiplayer.server.model.cards.projectile.Projectile;
import io.github.java_lan_multiplayer.server.model.tiles.*;

import java.awt.Point;
import java.util.*;
import java.util.function.Predicate;

import static io.github.java_lan_multiplayer.server.model.tiles.CrewType.*;

/**
 * Represents a 5x7 ship board on which {@link Tile} objects are placed.
 * <p>
 * Handles logic for tile placement, removal, crew and alien management,
 * battery and shield systems, cargo handling, and strength calculations.
 * <p>
 * Each ship is initialized with a {@link StartingCabinTile} at the center
 * and can be configured based on its {@link BoardType}.
 */
public class ShipBoard {

    private final Tile[][] tiles;
    private final BoardType shipType;
    private int removedTiles;

    /**
     * Constructs a new ship board of a given type and initializes the grid with empty tiles,
     * placing the {@link StartingCabinTile} at the center.
     *
     * @param shipType The type of ship board to create.
     */
    public ShipBoard(BoardType shipType) {
        this.tiles = new Tile[5][7];
        this.shipType = shipType;
        initializeShip();
        removedTiles = 0;
    }

    /**
     * Initializes the ship board by filling it with {@link EmptyTile} and placing the
     * {@link StartingCabinTile} in the center.
     */
    private void initializeShip() {
        // Empties the ship and places the starting cabin.
        for(int i = 0; i < tiles.length; i++) {
            for(int j = 0; j < tiles[i].length; j++) {
                tiles[i][j] = new EmptyTile();
            }
        }

        tiles[2][3] = new StartingCabinTile();
    }

    /**
     * Returns the 2D array of tiles on the ship.
     *
     * @return A 2D array representing the ship's tile grid.
     */
    public Tile[][] getTiles() {
        return tiles;
    }
    /**
     * Returns the number of tiles that have been removed from the ship.
     *
     * @return The number of removed tiles.
     */
    public int getRemovedTilesCount() {
        return removedTiles;
    }
    /**
     * Adds to the count of removed tiles.
     *
     * @param amount Number of tiles to add to the removed count.
     */
    public void addToRemovedTiles(int amount) {
        removedTiles += amount;
    }


    /**
     * Returns the tile at the specified (x, y) coordinates.
     *
     * @param x The x-coordinate (column).
     * @param y The y-coordinate (row).
     * @return The tile at the given position.
     * @throws IllegalArgumentException If the coordinates are outside the board.
     */
    public Tile getTile(int x, int y) {
        if(!isInsideShip(x, y)) throw new IllegalArgumentException("Invalid coordinates: ("+x+","+y+") is outside the ship board.");
        return tiles[y][x];
    }

    /**
     * Places a tile on the board using its tile ID and desired rotation.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param tileId The ID of the tile to place.
     * @param rotation The rotation to apply to the tile.
     */
    public void setTile(int x, int y, int tileId, int rotation) {
        // Places one of the 152 default tiles.
        Tile tile = TileFactory.getInstance().getTile(tileId);
        placeTile(x, y, tile, rotation);
    }
    /**
     * Places a provided tile at the specified position and rotation.
     * Used for testing and custom setups.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param tile The tile to place.
     * @param rotation The rotation to apply.
     */
    public void setTile(int x, int y, Tile tile, int rotation) {
        // Places the passed tile (used for testing).
        placeTile(x, y, tile, rotation);
    }
    /**
     * Core logic to place a tile on the board, validating position and constraints.
     * Updates alien support configuration if applicable.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param tile The tile to place.
     * @param rotation Rotation in 90° steps.
     */
    private void placeTile(int x, int y, Tile tile, int rotation) {
        if(tile instanceof EmptyTile || tile instanceof StartingCabinTile) throw new IllegalArgumentException("Cannot manually place tile type: " + tile.getTileType());
        if(!isInsideShip(x, y)) throw new IllegalArgumentException("Invalid coordinates: ("+x+","+y+") is outside the ship board.");
        if(tiles[y][x].getClass() != EmptyTile.class) throw new IllegalArgumentException("Tile occupied.");
        tiles[y][x] = tile;
        tiles[y][x].setRotation(rotation);
        if(tile instanceof CabinTile || tile instanceof AlienSupportTile) updateAlienSupport();
    }
    /**
     * Removes a tile from the board and updates alien support and connectivity.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public void removeTile(int x, int y) {
        tiles[y][x] = new EmptyTile();

        updateAlienSupport();
        // Checks if the removal of this tile left other tiles not connected to the ship.
        removedTiles++;
        updateConnectivity();
    }

    // BATTERY TILES
    /**
     * Returns the total number of stored batteries across all {@link BatteriesTile}s on the ship.
     *
     * @return The total battery count.
     */
    public int getTotalBatteriesCount() {
        int count = 0;
        for(Tile[] row : tiles) {
            for(Tile tile : row) {
                if(tile instanceof BatteriesTile batteries) count += batteries.getStoredBatteries();
            }
        }
        return count;
    }
    /**
     * Returns the number of stored batteries in the {@link BatteriesTile} at the given coordinates.
     * Primarily intended for debugging or testing.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return The number of stored batteries.
     * @throws IllegalArgumentException If the tile is not a {@code BatteriesTile}.
     */
    public int getBatteriesOf(int x, int y) {
        // for debugging.
        if(!isInsideShip(x, y)) throw new IllegalArgumentException("Invalid coordinates: ("+x+","+y+").");
        if(!(tiles[y][x] instanceof BatteriesTile)) throw new IllegalArgumentException("Tile at ("+x+","+y+") is not a BatteriesTile.");
        return ((BatteriesTile)tiles[y][x]).getStoredBatteries();
    }
    /**
     * Attempts to remove a battery from the {@link BatteriesTile} at the specified coordinates.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return {@code true} if a battery was removed; {@code false} if none were available.
     * @throws IllegalArgumentException If the tile is not a {@code BatteriesTile}.
     */
    public boolean removeBatteryFrom(int x, int y) {
        if(!isInsideShip(x, y)) throw new IllegalArgumentException("Invalid coordinates: ("+x+","+y+").");
        if(!(tiles[y][x] instanceof BatteriesTile)) throw new IllegalArgumentException("Tile at ("+x+","+y+") is not a BatteriesTile.");
        return ((BatteriesTile)tiles[y][x]).removeBattery();
    }

    /**
     * Removes one battery from each {@link Point} in the provided list.
     *
     * @param coordinates List of coordinate pairs representing battery tile positions.
     * @throws IllegalArgumentException If any coordinate does not have a removable battery.
     */
    public void removeBatteries(List<Point> coordinates) {
        for(Point tile : coordinates) {
            if(!removeBatteryFrom(tile.x, tile.y)) throw new IllegalArgumentException("No battery found at " + tile);
        }
    }

    // SHIELD TILES
    /**
     * Checks whether any {@link ShieldTile} is oriented such that it can block damage from a given side.
     *
     * @param side The side to check (0 = top, 1 = right, 2 = bottom, 3 = left).
     * @return {@code true} if a shield can protect that side, {@code false} otherwise.
     */
    public boolean isSideShielded(int side) {
        if(getTotalBatteriesCount() == 0) return false;
        for(Tile[] row : tiles) {
            for(Tile tile : row) {
                if(tile instanceof ShieldTile shield)
                    // A non rotated shield can protect the top and right side.
                    if(shield.getRotation() == side || shield.getRotation() == side-1) return true;
            }
        }
        return false;
    }

    // CABIN TILES
    /**
     * Calculates the total number of humans on the ship across all cabin tiles.
     *
     * @return Total number of human crew members.
     */
    public int getTotalHumanCount() {
        int count = 0;
        for(Tile[] row : tiles) {
            for(Tile tile : row) {
                if(tile instanceof CabinTile cabin) count += cabin.getHumanCount();
            }
        }
        return count;
    }
    /**
     * Calculates the total number of crew members, including aliens.
     *
     * @return Total crew count.
     */
    public int getTotalCrewCount() {
        int count = getTotalHumanCount();
        if(hasAlien(BROWN_ALIEN)) count++;
        if(hasAlien(PURPLE_ALIEN)) count++;
        return count;
    }
    /**
     * Checks if the ship currently has a specific type of alien on board.
     *
     * @param alienType The type of alien to check for.
     * @return {@code true} if the alien type is present, {@code false} otherwise.
     */
    public boolean hasAlien(CrewType alienType) {
        for(Tile[] row : tiles) {
            for(Tile tile : row) {
                if(tile instanceof CabinTile cabin && cabin.hasAlien(alienType)) return true;
            }
        }
        return false;
    }
    /**
     * Checks if a specific alien type is present at a specific cabin tile.
     * Intended for testing/debugging.
     *
     * @param x         The x-coordinate.
     * @param y         The y-coordinate.
     * @param alienType The alien type to check.
     * @return {@code true} if the alien is present at the tile.
     */
    public boolean hasAlienAt(int x, int y, CrewType alienType) {
        // for debugging
        // For tests, maybe I'll remove it later
        if(!isInsideShip(x, y)) throw new IllegalArgumentException("Invalid coordinates: ("+x+","+y+").");
        if(!(tiles[y][x] instanceof CabinTile)) throw new IllegalArgumentException("Tile at ("+x+","+y+") is not a Cabin.");
        return ((CabinTile) tiles[y][x]).hasAlien(alienType);
    }
    /**
     * Sets the crew type at a specific cabin tile, enforcing the rule
     * that only one of each alien type can exist on the ship.
     * Intended for testing.
     *
     * @param x        The x-coordinate.
     * @param y        The y-coordinate.
     * @param crewType The crew type to set.
     */
    public void setCrewTypeAt(int x, int y, CrewType crewType) {
        // used in tests for easy crew setup
        if(!isInsideShip(x, y)) throw new IllegalArgumentException("Invalid coordinates: ("+x+","+y+").");
        if(!(tiles[y][x] instanceof CabinTile)) throw new IllegalArgumentException("Tile at ("+x+","+y+") is not a Cabin.");
        if(crewType.isAlien() && hasAlien(crewType)) {
            for(Tile[] row : tiles) {
                for(Tile tile : row) {
                    if(tile instanceof CabinTile cabin && cabin.hasAlien(crewType)) {
                        cabin.setCrewType(DOUBLE_HUMAN);
                    }
                }
            }
        }
        ((CabinTile)tiles[y][x]).setCrewType(crewType);
    }
    /**
     * Cycles the crew type at a specific cabin tile through valid types,
     * ensuring alien uniqueness rules are enforced.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public void cycleCrewTypeAt(int x, int y) {
        if (!isInsideShip(x, y)) throw new IllegalArgumentException("Invalid coordinates: (" + x + "," + y + ").");
        if (!(tiles[y][x] instanceof CabinTile cabin)) throw new IllegalArgumentException("Tile at (" + x + "," + y + ") is not a Cabin.");

        CrewType current = cabin.getCrew();
        List<CrewType> cycleOptions = cabin.getValidCrewTypesForCycle();

        // Cycle to the next one
        int nextIndex = (cycleOptions.indexOf(current) + 1) % cycleOptions.size();
        CrewType next = cycleOptions.get(nextIndex);

        // Enforce alien uniqueness if applicable
        if (next.isAlien() && hasAlien(next)) {
            // Remove existing alien of that type
            for (Tile[] row : tiles) {
                for (Tile t : row) {
                    if (t instanceof CabinTile otherCabin && otherCabin.hasAlien(next)) {
                        otherCabin.setCrewType(DOUBLE_HUMAN);
                    }
                }
            }
        }
        cabin.setCrewType(next);
    }
    /**
     * Removes one crew member from a specific cabin tile.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return The {@link CrewType} that was removed.
     */
    public CrewType removeCrewMemberAt(int x, int y) {
        if(!isInsideShip(x, y)) throw new IllegalArgumentException("Invalid coordinates: ("+x+","+y+").");
        if(!(tiles[y][x] instanceof CabinTile cabin)) throw new IllegalArgumentException("Tile at ("+x+","+y+") is not a Cabin.");
        return cabin.removeMember();
    }
    /**
     * Applies the effect of the Epidemic card, removing one crew member
     * from each affected cabin that is adjacent to another occupied cabin.
     */
    public void applyEpidemicCard() {
        Set<CabinTile> infectedCabins = new HashSet<>();

        for(int y = 0; y < tiles.length; y++) {
            for(int x = 0; x < tiles[0].length; x++) {
                if(!isInsideShip(x, y)) continue;

                if(tiles[y][x] instanceof CabinTile cabin && cabin.hasCrew()) {
                    int[][] offsets = { {-1, 0}, {0, 1}, {1, 0}, {0, -1} };

                    for(int i = 0; i < 4; i++) {
                        if(cabin.getConnector(i) == Connector.NONE) continue;
                        int nx = x + offsets[i][1];
                        int ny = y + offsets[i][0];
                        if(!isInsideShip(nx, ny)) continue;

                        if(tiles[ny][nx] instanceof CabinTile neighbour && neighbour.hasCrew()) infectedCabins.add(cabin);
                    }
                }
            }
        }
        for(CabinTile infected : infectedCabins) {
            infected.removeMember();
        }
    }

    // ACTIVATABLE TILES
    /**
     * Computes the effective strength of a given activatable tile type,
     * factoring in the number of double vertical and rotated tiles.
     *
     * @param tileClass        The class of activatable tile.
     * @param doubleVertical   Number of vertical double tiles activated.
     * @param doubleRotated    Number of rotated double tiles activated.
     * @return The computed strength.
     */
    public double getStrength(Class<? extends ActivatableTile> tileClass, int doubleVertical, int doubleRotated) {
        return StrengthCalculator.calculateStrength(this, tileClass, doubleVertical, doubleRotated);
    }
    /**
     * Computes strength assuming no rotated double tiles are activated.
     *
     * @param tileClass      The tile type.
     * @param doubleVertical Number of vertical double tiles activated.
     * @return Computed strength.
     */
    public double getStrength(Class<? extends ActivatableTile> tileClass, int doubleVertical) {
        return StrengthCalculator.calculateStrength(this, tileClass, doubleVertical, 0);
    }

    /**
     * Gets the minimum possible strength of a given activatable tile type.
     *
     * @param tileClass The class of tile.
     * @return Minimum strength value.
     */
    public double getMinStrength(Class<? extends ActivatableTile> tileClass) {
        return getStrength(tileClass, 0, 0);
    }
    /**
     * Gets the maximum possible strength of a given activatable tile type,
     * based on available batteries and tile orientations.
     *
     * @param tileClass The class of tile.
     * @return Maximum achievable strength.
     */
    public double getMaxStrength(Class<? extends ActivatableTile> tileClass) {
        int batteries = getTotalBatteriesCount();

        int doubleVerticalAvailable = getTileCount(tileClass, tile -> ((ActivatableTile) tile).isDouble() && tile.getRotation() == 0);
        int doubleVertical = Math.min(batteries, doubleVerticalAvailable);
        batteries -= doubleVertical;

        int doubleRotatedAvailable = getTileCount(tileClass, tile -> ((ActivatableTile) tile).isDouble() && tile.getRotation() != 0);
        int doubleRotated = Math.min(batteries, doubleRotatedAvailable);

        return getStrength(tileClass, doubleVertical, doubleRotated);
    }

    /**
     * Counts the total number of connectors that are exposed to space (i.e. adjacent to empty space or out of bounds).
     *
     * @return Number of exposed connectors.
     */
    public int getExposedConnectorsCount() {
        int count = 0;
        int[][] offsets = { {-1, 0}, {0, 1}, {1, 0}, {0, -1} };
        for(int y = 0; y < tiles.length; y++) {
            for(int x = 0; x < tiles[0].length; x++) {
                if(!(tiles[y][x] instanceof EmptyTile)) {
                    for(int i = 0; i < 4; i++) {
                        int nx = x + offsets[i][1];
                        int ny = y + offsets[i][0];
                        if(tiles[y][x].getConnector(i) != Connector.NONE) {
                            if(!isInsideShip(nx, ny)) count++;
                            else if(tiles[ny][nx] instanceof EmptyTile) count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    // CARGO TILES
    /**
     * Loads a cargo block into a cargo tile at a given slot.
     *
     * @param x         The x-coordinate.
     * @param y         The y-coordinate.
     * @param index     Slot index to load into.
     * @param blockType The block to load.
     * @return The previous block in that slot, if any.
     */
    public BlockType loadCargoAt(int x, int y, int index, BlockType blockType) {
        if(!isInsideShip(x, y)) throw new IllegalArgumentException("Invalid coordinates: ("+x+","+y+").");
        if(!(tiles[y][x] instanceof CargoTile cargo)) throw new IllegalArgumentException("Tile at ("+x+","+y+") is not a Cargo.");
        return cargo.loadBlock(index, blockType);
    }
    /**
     * Unloads a cargo block from a specific cargo tile slot.
     *
     * @param x     The x-coordinate.
     * @param y     The y-coordinate.
     * @param index The slot index to unload.
     * @return The block removed.
     */
    public BlockType unloadCargoAt(int x, int y, int index) {
        if(!isInsideShip(x, y)) throw new IllegalArgumentException("Invalid coordinates: ("+x+","+y+").");
        if(!(tiles[y][x] instanceof CargoTile cargo)) throw new IllegalArgumentException("Tile at ("+x+","+y+") is not a Cargo.");
        return cargo.unloadBlock(index);
    }

    /**
     * Computes the total value of all cargo currently stored on the ship.
     *
     * @return Total cargo value.
     */
    public int getTotalHoldsValue() {
        int count = 0;
        for(Tile[] row : tiles) {
            for(Tile tile : row) {
                if(tile instanceof CargoTile cargo) count += cargo.getHoldsValue();
            }
        }
        return count;
    }
    /**
     * Removes the most valuable cargo blocks (and then batteries if needed)
     * to satisfy a penalty requiring cargo removal.
     *
     * @param quantity The number of valuable units to remove.
     */
    public void removeMostValuableCargo(int quantity) {
        class BlockInfo {
            final CargoTile tile;
            final int index;
            final int value;
            BlockInfo(CargoTile tile, int index, int value) {
                this.tile = tile;
                this.index = index;
                this.value = value;
            }
        }
        List<BlockInfo> cargoBlocks = new ArrayList<>();
        for(Tile[] row : tiles) {
            for(Tile tile : row) {
                if(tile instanceof CargoTile cargo) {
                    for(int index = 0; index < cargo.getCapacity(); index++) {
                        BlockType block = cargo.getBlockAt(index);
                        if(block != null) {
                            cargoBlocks.add(new BlockInfo(cargo, index, block.getValue()));
                        }
                    }
                }
            }
        }
        cargoBlocks.sort((a, b) -> Integer.compare(b.value, a.value));
        int remainingToRemove = quantity;
        for(BlockInfo cargo: cargoBlocks) {
            if(!(remainingToRemove > 0)) break;
            cargo.tile.unloadBlock(cargo.index);
            remainingToRemove--;
        }
        if(remainingToRemove > 0) {
            for(Tile[] row : tiles) {
                for(Tile tile : row) {
                    if(tile instanceof BatteriesTile batteryTile) {
                        while(remainingToRemove > 0 && batteryTile.getStoredBatteries() > 0) {
                            batteryTile.removeBattery();
                            remainingToRemove--;
                        }
                    }
                    if(remainingToRemove == 0) return;
                }
            }
        }
    }

    /**
     * Returns the positions of all tiles of a given class that match a given predicate.
     *
     * @param tileClass The tile class to filter by.
     * @param filter    A predicate to match specific tile conditions.
     * @return A set of points representing matching tile positions.
     */
    private Set<Point> getTilesPositions(Class<? extends Tile> tileClass, Predicate<Tile> filter) {
        Set<Point> positions = new HashSet<>();
        for(int y = 0; y < tiles.length; y++) {
            for(int x = 0; x < tiles[0].length; x++) {
                Tile tile = tiles[y][x];
                if(tileClass.isInstance(tile) && filter.test(tile)) {
                    positions.add(new Point(x, y));
                }
            }
        }
        return positions;
    }
    /**
     * Returns positions of all cabin tiles that can host alien crew members.
     *
     * @return Set of valid cabin tile positions.
     */
    public Set<Point> getCabinTilesThatCanHostAliens() {
        return getTilesPositions(CabinTile.class, tile -> ((CabinTile)tile).canHostAnAlienType());
    }
    /**
     * Returns positions of all double cannon tiles oriented vertically or not.
     *
     * @param vertical If {@code true}, selects vertical tiles.
     * @return Set of matching tile positions.
     */
    public Set<Point> getDoubleCannonsPositions(boolean vertical) {
        return getTilesPositions(CannonTile.class, tile -> ((CannonTile)tile).isDouble() && vertical == (tile.getRotation() == 0));
    }
    /**
     * Returns positions of all double engine tiles on the ship.
     *
     * @return Set of double engine tile coordinates.
     */
    public Set<Point> getDoubleEnginesPositions() {
        return getTilesPositions(EngineTile.class, tile -> ((EngineTile)tile).isDouble());
    }

    public record BatteriesData(int x, int y, int batteries, int capacity, int rotation) {}
    public record CrewData(int x, int y, CrewType crew) {}
    public record CargoData(int x, int y, BlockType[] cargo, int capacity, int rotation) {}
    /**
     * Record representing the complete current state of key ship contents.
     *
     * @param batteriesData Set of batteries tile data.
     * @param crewData      Set of crew tile data.
     * @param cargoData     Set of cargo tile data.
     */
    public record ShipContent(
            Set<BatteriesData> batteriesData,
            Set<CrewData> crewData,
            Set<CargoData> cargoData
    ) {}
    /**
     * Gathers the current state of all batteries, crew, and cargo tiles on the ship.
     *
     * @return A {@link ShipContent} record representing the ship state.
     */
    public ShipContent getShipContent() {
        Set<BatteriesData> batteries = new HashSet<>();
        Set<CrewData> crew = new HashSet<>();
        Set<CargoData> cargo = new HashSet<>();

        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {
                Tile tile = tiles[y][x];

                if (tile instanceof BatteriesTile batteryTile) {
                    batteries.add(new BatteriesData(x, y, batteryTile.getStoredBatteries(), batteryTile.getCapacity(), batteryTile.getRotation()));
                } else if (tile instanceof CabinTile cabin) {
                    crew.add(new CrewData(x, y, cabin.getCrew()));
                } else if (tile instanceof CargoTile cargoTile) {
                    cargo.add(new CargoData(x, y, cargoTile.getContent(), cargoTile.getCapacity(), cargoTile.getRotation()));
                }
            }
        }
        return new ShipContent(batteries, crew, cargo);
    }

    /**
     * Counts the number of tiles of a given class that satisfy the specified predicate.
     *
     * @param tileClass The class of tiles to count.
     * @param filter    Additional condition to check for each tile.
     * @return Number of matching tiles.
     */
    public int getTileCount(Class<? extends Tile> tileClass, Predicate<Tile> filter) {
        int count = 0;
        for(Tile[] row : tiles) {
            for(Tile tile : row) {
                if(tileClass.isInstance(tile) && filter.test(tile)) count++;
            }
        }
        return count;
    }
    /**
     * Counts all non-empty tiles currently on the ship.
     *
     * @return Total tile count (excluding empty tiles).
     */
    public int getTotalTileCount() {
        return getTileCount(Tile.class, tile -> !(tile instanceof EmptyTile));
    }

    /**
     * Checks whether a tile of a specific class and matching a given filter exists along a specified path.
     *
     * @param source     The direction from which the path is checked (UP, DOWN, LEFT, RIGHT).
     * @param index      The index of the path (column or row depending on direction).
     * @param tileClass  The class of tile to look for.
     * @param filter     A predicate to apply to tiles of the specified class.
     * @return {@code true} if a matching tile exists on the path; {@code false} otherwise.
     */
    public boolean hasTileOnPath(Projectile.Source source, int index, Class<? extends Tile> tileClass, Predicate<Tile> filter) {
        if(source == Projectile.Source.UP || source == Projectile.Source.DOWN) {
            if(index < 0 || index >= tiles[0].length) return false;
            for(Tile[] row : tiles) {
                if(tileClass.isInstance(row[index]) && filter.test(row[index])) return true;
            }
        }
        else if(source == Projectile.Source.LEFT || source == Projectile.Source.RIGHT) {
            if(index < 0 || index >= tiles.length) return false;
            for(Tile tile : tiles[index]) {
                if(tileClass.isInstance(tile) && filter.test(tile)) return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the specified path is empty (i.e., contains only {@link EmptyTile} instances).
     *
     * @param source The direction from which the path is checked.
     * @param index  The index of the path (column or row depending on direction).
     * @return {@code true} if the path contains only empty tiles; {@code false} otherwise.
     */
    public boolean isPathEmpty(Projectile.Source source, int index) {
        return !hasTileOnPath(source, index, Tile.class, tile -> !(tile instanceof EmptyTile));
    }
    /**
     * Enum representing the type of defending cannon present along a path.
     */
    public enum DefendingCannonType {
        NO_CANNON, SINGLE, DOUBLE
    }
    /**
     * Determines the type of defending cannon available on the specified path.
     * <p>
     * A single cannon is valid if it matches the direction.
     * A double cannon is valid only if there is at least one battery and the cannon matches the direction.
     * </p>
     *
     * @param source The direction from which the attack is expected.
     * @param index  The index of the path (column or row depending on direction).
     * @return A {@link DefendingCannonType} indicating whether the path has a single, double, or no cannon.
     */
    public DefendingCannonType getDefendingCannonType(Projectile.Source source, int index) {
        if(source == Projectile.Source.UP) {
            if(hasTileOnPath(source, index, CannonTile.class, tile -> tile.getRotation() == source.toInt() && !((CannonTile)tile).isDouble())) return DefendingCannonType.SINGLE;
            if(getTotalBatteriesCount() > 0 &&
                    hasTileOnPath(source, index, CannonTile.class, tile -> tile.getRotation() == source.toInt() && ((CannonTile)tile).isDouble())) return DefendingCannonType.DOUBLE;
        }
        else {
            for(int i = -1; i <= 1; i++) {
                if(hasTileOnPath(source, index + i, CannonTile.class, tile -> tile.getRotation() == source.toInt() && !((CannonTile)tile).isDouble()))
                    return DefendingCannonType.SINGLE;
            }
            for(int i = -1; i <= 1; i++) {
                if(getTotalBatteriesCount() > 0 &&
                        hasTileOnPath(source, index + i, CannonTile.class, tile -> tile.getRotation() == source.toInt() && ((CannonTile)tile).isDouble()))
                    return DefendingCannonType.DOUBLE;
            }
        }
        return DefendingCannonType.NO_CANNON;
    }

    /**
     * Returns the position of the first non-empty tile on a given path, based on projectile direction and index.
     *
     * @param source the direction from which the projectile is coming
     * @param index the index along the path (column or row depending on direction)
     * @return the Point of the first non-empty tile or null if the path is empty
     */
    public Point getFirstNonEmptyTilePosition(Projectile.Source source, int index) {
        if(isPathEmpty(source, index)) return null;

        if(source == Projectile.Source.UP) {
            for(int y = 0; y < tiles.length; y++) {
                if(!(tiles[y][index] instanceof EmptyTile)) return new Point(index, y);
            }
        } else if(source == Projectile.Source.LEFT) {
            for(int x = 0; x < tiles[0].length; x++) {
                if(!(tiles[index][x] instanceof EmptyTile)) return new Point(x, index);
            }
        } else if(source == Projectile.Source.DOWN) {
            for(int y = tiles.length - 1; y >= 0; y--) {
                if(!(tiles[y][index] instanceof EmptyTile)) return new Point(index, y);
            }
        } else if(source == Projectile.Source.RIGHT) {
            for(int x = tiles[0].length - 1; x >= 0; x--) {
                if(!(tiles[index][x] instanceof EmptyTile)) return new Point(x, index);
            }
        }
        // Should not be able to reach here.
        throw new RuntimeException("Error while getting first non empty tile position");
    }
    /**
     * Removes the first non-empty tile encountered along a projectile path.
     *
     * @param source the direction from which the projectile is coming
     * @param index the index along the path
     * @return the position of the removed tile, or null if none were removed
     */
    public Point removeFirstTileInPath(Projectile.Source source, int index) {
        Point pos = getFirstNonEmptyTilePosition(source, index);
        if(pos != null) removeTile(pos.x, pos.y);
        return pos;
    }
    /**
     * Checks if the first tile on the projectile path has a connector exposed in the projectile's direction.
     *
     * @param source the direction from which the projectile is coming
     * @param index the index along the path
     * @return true if there is an exposed connector, false otherwise
     */
    public boolean hasExposedConnectorInPath(Projectile.Source source, int index) {
        Point pos = getFirstNonEmptyTilePosition(source, index);
        if(pos == null) return false;
        return getTile(pos.x, pos.y).getConnector(source.toInt()) != Connector.NONE;
    }
    /**
     * Determines if two connectors are compatible (can be joined together).
     *
     * @param a the first connector
     * @param b the second connector
     * @return true if the connectors are compatible, false otherwise
     */
    private boolean areConnectorsCompatible(Connector a, Connector b) {
        if(a == Connector.NONE && b == Connector.NONE) return true;
        if(a == Connector.NONE || b == Connector.NONE) return false;
        if(a == b) return true;
        return a == Connector.UNIVERSAL || b == Connector.UNIVERSAL;
    }

    private boolean hasValidConnections(int x, int y) {
        // Checks the connection in all 4 directions to see if its compatible.
        Tile tile = getTile(x, y);
        int[][] offsets = { {-1, 0}, {0, 1}, {1, 0}, {0, -1} };
        for(int side = 0; side < 4; side++) {
            int neighbor_x = x + offsets[side][1];
            int neighbor_y = y + offsets[side][0];

            if(!isInsideShip(neighbor_x, neighbor_y)) continue;

            Tile neighbor = getTile(neighbor_x, neighbor_y);
            if(neighbor instanceof EmptyTile) continue;

            int opposite_side = (side + 2) % 4;
            if(!areConnectorsCompatible(tile.getConnector(side), neighbor.getConnector(opposite_side))) return false;
        }
        return true;
    }

    /**
     * Verifies whether a coordinate lies within the usable bounds of the ship.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return true if the coordinate is valid within the current ship type, false otherwise
     */
    protected boolean isInsideShip(int x, int y) {
        // Returns true if(x,y) is inside the usable space of the spaceship.
        if(y < 0 || x < 0 || y >= tiles.length || x >= tiles[0].length) return false;
        return isCellValid(x, y, shipType);
    }

    /**
     * Static utility to check if a specific cell is valid for tile placement based on ship type.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param shipType the board type
     * @return true if the cell is valid, false otherwise
     */
    public static boolean isCellValid(int x, int y, BoardType shipType) {
        if(shipType == BoardType.LEARNING || shipType == BoardType.LEVEL_ONE) {
            int[][] type1 = {
                    {0, 0, 0, 1, 0, 0, 0},
                    {0, 0, 1, 1, 1, 0, 0},
                    {0, 1, 1, 1, 1, 1, 0},
                    {0, 1, 1, 1, 1, 1, 0},
                    {0, 1, 1, 0, 1, 1, 0}
            };
            return type1[y][x] == 1;
        }
        if(shipType == BoardType.LEVEL_TWO) {
            int[][] type2 = {
                    {0, 0, 1, 0, 1, 0, 0},
                    {0, 1, 1, 1, 1, 1, 0},
                    {1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 0, 1, 1, 1}
            };
            return type2[y][x] == 1;
        }
        throw new RuntimeException("Invalid ship type: " + shipType);
    }

    /**
     * Updates cabin tiles to allow them to host specific alien types if
     * adjacent alien support tiles are present.
     */
    private void updateAlienSupport() {
        // Checks if a cabin and an alien support are placed adjacent one another,
        // if so, it allows the cabin to host that type of alien.
        for(int y = 0; y < tiles.length; y++) {
            for(int x = 0; x < tiles[0].length; x++) {

                if(!isInsideShip(x, y)) continue;
                if(tiles[y][x] instanceof CabinTile cabin && !(cabin instanceof StartingCabinTile)) {
                    boolean canHostBrown = false;
                    boolean canHostPurple = false;
                    int[][] offsets = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

                    for(int i = 0; i < 4; i++) {
                        if(cabin.getConnector(i) == Connector.NONE) continue;
                        int nx = x + offsets[i][1];
                        int ny = y + offsets[i][0];
                        if(!isInsideShip(nx, ny)) continue;

                        if(tiles[ny][nx] instanceof AlienSupportTile support) {
                            if(support.getAlienColor() == BROWN_ALIEN) canHostBrown = true;
                            if(support.getAlienColor() == PURPLE_ALIEN) canHostPurple = true;
                        }
                    }
                    cabin.allowAlienType(BROWN_ALIEN, canHostBrown);
                    cabin.allowAlienType(PURPLE_ALIEN, canHostPurple);
                }
            }
        }
    }

    /**
     * Returns a set of all tile positions that are currently invalid due to:
     * - Invalid rotation of engine tiles
     * - Engine or cannon tile outputs facing into other tiles
     * - Improper or incompatible connector alignments
     *
     * @return a Set of Points representing invalid tile positions
     */
    public Set<Point> getInvalidTiles() {
        // Adds all invalid tile's coordinates to invalidTiles.
        Set<Point> invalidTiles = new HashSet<>();

        for(int y = 0; y < tiles.length; y++) {
            for(int x = 0; x < tiles[0].length; x++) {
                if(!isInsideShip(x, y)) continue;
                Tile tile = getTile(x, y);
                if(tile instanceof EmptyTile) continue;
                if(tile instanceof EngineTile engine) {
                    if(engine.getRotation() != 0) {
                        invalidTiles.add(new Point(x, y));
                        continue;
                    }
                }
                if(tile instanceof EngineTile || tile instanceof CannonTile) {
                    int tileRotation = tile.getRotation();
                    if(tile instanceof EngineTile) tileRotation = (tileRotation + 2) % 4;

                    int[][] offsets = { {-1, 0}, {0, 1}, {1, 0}, {0, -1} };
                    int nx = x + offsets[tileRotation][1];
                    int ny = y + offsets[tileRotation][0];
                    if(isInsideShip(nx, ny) && !(tiles[ny][nx] instanceof EmptyTile)) {
                        invalidTiles.add(new Point(x, y));
                        invalidTiles.add(new Point(nx, ny));
                        continue;
                    }
                }
                if(!hasValidConnections(x,y)) {
                    invalidTiles.add(new Point(x, y));
                }
            }
        }
        //checkConnectivity();
        return invalidTiles;
    }

    /**
     * Updates the ship's connectivity groups using flood fill.
     * Removes disconnected groups without humans, and if multiple human groups
     * exist, retains only the largest.
     */
    public void updateConnectivity() {
        // It uses a flood fill search to divide all the tiles in groups.
        // If there are more than one group, it automatically removes the ones without humans
        // and lets the player decide which one (with humans) to keep.
        boolean[][] visited = new boolean[tiles.length][tiles[0].length];

        List<List<Point>> groupsWithHumans = new ArrayList<>();
        List<List<Point>> groupsWithoutHumans = new ArrayList<>();
        for(int y = 0; y < tiles.length; y++) {
            for(int x = 0; x < tiles[0].length; x++) {
                if(!visited[y][x] && !(tiles[y][x] instanceof EmptyTile)) {
                    List<Point> group = new ArrayList<>();
                    floodFill(x, y, visited, group);
                    if(groupHasHumans(group)) {
                        groupsWithHumans.add(group);
                    } else {
                        groupsWithoutHumans.add(group);
                    }
                }
            }
        }
        if(groupsWithHumans.size() + groupsWithoutHumans.size() <= 1) return;

        // If there are both groups with crew and without humans, it removes the ones without.
        // It doesn't always delete the empty groups because if the last humans are removed, it would delete the ship.
        if(!groupsWithHumans.isEmpty()) {
            for(List<Point> group : groupsWithoutHumans) {
                removeGroup(group);
            }
            if(groupsWithHumans.size() > 1) {
                //groupsToChooseFrom = groupsWithHumans;

                // Find the group with the maximum number tiles
                List<Point> largestGroup = null;
                int maxSize = -1;
                for (List<Point> group : groupsWithHumans) {
                    if (group.size() > maxSize) {
                        largestGroup = group;
                        maxSize = group.size();
                    }
                }

                // Remove all other human groups except the largest
                for (List<Point> group : groupsWithHumans) {
                    if (group != largestGroup) {
                        removeGroup(group);
                    }
                }
            }
        }
    }

    /**
     * Performs a flood fill starting from the given tile, adding all connected
     * tiles with valid connectors into a single group.
     *
     * @param x starting x-coordinate
     * @param y starting y-coordinate
     * @param visited matrix to keep track of visited tiles
     * @param group the group to add tiles into
     */
    private void floodFill(int x, int y, boolean[][] visited, List<Point> group) {
        // Algorithm to recursively add all connected tiles to the same group.
        if(!isInsideShip(x, y)) return;
        if(visited[y][x] || tiles[y][x] instanceof EmptyTile) return;

        visited[y][x] = true;
        group.add(new Point(x, y));
        int[][] offsets = { {-1, 0}, {0, 1}, {1, 0}, {0, -1} };
        for(int i = 0; i < 4; i++) {
            if(tiles[y][x].getConnector(i) == Connector.NONE) continue;
            int nx = x + offsets[i][1];
            int ny = y + offsets[i][0];
            floodFill(nx, ny, visited, group);
        }
    }

    /**
     * Checks whether a given group of tiles contains at least one cabin tile
     * with a human inside.
     *
     * @param group a list of Points representing a group of tiles
     * @return true if the group contains humans, false otherwise
     */
    private boolean groupHasHumans(List<Point> group) {
        for(Point p : group) {
            if(tiles[p.y][p.x] instanceof CabinTile cabin) {
                if(cabin.getHumanCount() >= 1) return true;
            }
        }
        return false;
    }

    /**
     * Removes all tiles in the specified group by replacing them with EmptyTiles.
     *
     * @param group a list of Points representing the tiles to remove
     */
    private void removeGroup(List<Point> group) {
        // It clears all tiles of a specific group (sets them to EmptyTile).
        for(Point p : group) {
            // I can directly set the removed tile to Empty,
            // there is no need to check alien support.
            tiles[p.y][p.x] = new EmptyTile();
            removedTiles++;
        }
    }
}