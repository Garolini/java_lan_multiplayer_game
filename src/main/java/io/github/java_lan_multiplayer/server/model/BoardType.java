package io.github.java_lan_multiplayer.server.model;

/**
 * Represents a predefined board configuration for different levels or flight types.
 * Each board defines its flight track length, number of timers, starting positions,
 * reward structure based on position, and bonus for the best ship.
 */
public enum BoardType {

    LEARNING  ("learningFlight", 18, 0, new int[] {4, 2, 1, 0}, new int[] {4, 3, 2, 1}, 2),
    LEVEL_ONE ("levelOne",       18, 2, new int[] {4, 2, 1, 0}, new int[] {4, 3, 2, 1}, 2),
    LEVEL_TWO ("levelTwo",       24, 3, new int[] {6, 3, 1, 0}, new int[] {8, 6, 4, 2}, 4);


    private final String displayName;
    private final int flightTrackLength;
    private final int buildingTimersCount;
    private final int[] startingPositions;
    private final int[] positionReward;
    private final int bestShipReward;

    BoardType(String displayName, int flightTrackLength, int buildingTimersCount, int[] startingPositions, int[] positionReward, int bestShipReward) {
        this.displayName = displayName;
        this.flightTrackLength = flightTrackLength;
        this.buildingTimersCount = buildingTimersCount;
        this.startingPositions = startingPositions;
        this.positionReward = positionReward;
        this.bestShipReward = bestShipReward;
    }

    /**
     * @return the length of the flight track for this board type
     */
    public int getFlightTrackLength() {
        return flightTrackLength;
    }

    /**
     * @return the number of building timers for this board type
     */
    public int getBuildingTimersCount() {
        return buildingTimersCount;
    }

    /**
     * Gets the starting position offset for a player at the given index.
     * @param positionIndex the 1-based index of the player (1 = first, 2 = second, etc.)
     * @return the number of steps from the start
     * @throws IllegalArgumentException if the index is out of range
     */
    public int getStartingPosition(int positionIndex) {
        if (positionIndex < 1 || positionIndex > startingPositions.length) {
            throw new IllegalArgumentException("Invalid position index: " + positionIndex);
        }
        return startingPositions[positionIndex - 1];
    }

    /**
     * Gets the reward for the player at a specific position.
     * @param positionIndex the 1-based finishing position (1 = first, 2 = second, etc.)
     * @return the credit reward
     * @throws IllegalArgumentException if the index is out of range
     */
    public int getPositionReward(int positionIndex) {
        if (positionIndex < 1 || positionIndex > positionReward.length) {
            throw new IllegalArgumentException("Invalid position index: " + positionIndex);
        }
        return positionReward[positionIndex - 1];
    }

    /**
     * @return the bonus reward for the player with the best ship
     */
    public int getBestShipReward() {
        return bestShipReward;
    }


    @Override
    public String toString() {
        return displayName;
    }
}
