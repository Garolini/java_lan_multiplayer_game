package io.github.java_lan_multiplayer.server.model;

import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.endGame.PlayerScores;
import io.github.java_lan_multiplayer.common.messages.lobby.BoardSelectMessage;
import io.github.java_lan_multiplayer.common.messages.login.JoinResponseMessage;
import io.github.java_lan_multiplayer.common.messages.login.PlayerInfo;
import io.github.java_lan_multiplayer.server.events.EventDispatcher;
import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.events.ModelEventListener;
import io.github.java_lan_multiplayer.server.events.flight.*;
import io.github.java_lan_multiplayer.server.events.shipBuilding.*;
import io.github.java_lan_multiplayer.server.events.flight.cards.CardPickedEvent;
import io.github.java_lan_multiplayer.server.events.game.GameResetEvent;
import io.github.java_lan_multiplayer.server.events.game.GameStateUpdateEvent;
import io.github.java_lan_multiplayer.server.events.gameOver.GameOverEvent;
import io.github.java_lan_multiplayer.server.events.lobby.BoardTypeUpdateEvent;
import io.github.java_lan_multiplayer.server.events.lobby.PlayersUpdateEvent;
import io.github.java_lan_multiplayer.server.model.cards.Card;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.github.java_lan_multiplayer.server.model.GameModel.GameState.*;
import static io.github.java_lan_multiplayer.server.model.Player.PlayerState.*;

/**
 * The {@code GameModel} class represents the state and behavior of the game.
 * It handles player management, tile and card pile allocation, deck generation, and game state transitions.
 * This class is responsible for most of the server-side game logic, including score computation,
 * board setup, and in-game player actions.
 */
public class GameModel {
    private BoardType boardType;
    private final List<Player> players;
    private List<Card> deck;
    private List<List<Card>> cardPiles;

    private final EventDispatcher eventDispatcher = new EventDispatcher();

    private final Set<Integer> availableTiles;
    private final Set<Integer> availableCardPiles;
    private final Set<Integer> availablePositions;

    private final Set<Player> invalidShips = new HashSet<>();
    private final Set<Player> choosingCrewType = new HashSet<>();

    private Card currentCard;

    private final BuildingTimerHandler timerHandler = new BuildingTimerHandler(80, eventDispatcher, this::onTimerFinished);
    private Boolean timerFlipAvailable = false;

    /**
     * Enumeration of all possible game states.
     */
    public enum GameState {
        LOBBY,
        BUILDING_SHIPS,
        CORRECTING_SHIP,
        ASSIGNING_CREW,
        PICKING_CARDS,
        WAITING
    }
    private GameState gameState = LOBBY;

    /**
     * Initializes a new {@code GameModel} with default values and an empty player list.
     */
    public GameModel() {
        this.players = new ArrayList<>();
        this.boardType = BoardType.LEVEL_TWO;
        this.availableTiles = new HashSet<>();
        for (int i = 0; i < 152; i++) {
            availableTiles.add(i);
        }
        this.availableCardPiles = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            availableCardPiles.add(i);
        }
        this.availablePositions = new HashSet<>();
        this.deck = new ArrayList<>();
    }

    /**
     * Returns the current state of the game.
     *
     * @return the current {@link GameState}.
     */
    public GameState getGameState() {
        return gameState;
    }

    public BoardType getBoardType() {
        return boardType;
    }
    public void setBoardType(BoardType boardType) {
        this.boardType = boardType;
    }
    /**
     * Cycles the board type to the next or previous type, depending on the action.
     * Fires a BoardTypeUpdateEvent when updated.
     *
     * @param action the direction to cycle (NEXT or PREVIOUS).
     */
    public void cycleBoard(BoardSelectMessage.Action action) {
        BoardType[] cycleTypes = BoardType.values();

        int currentIndex = Arrays.asList(cycleTypes).indexOf(boardType);

        int newIndex = switch (action) {
            case NEXT -> (currentIndex + 1) % cycleTypes.length;
            case PREVIOUS -> (currentIndex - 1 + cycleTypes.length) % cycleTypes.length;
        };

        this.boardType = cycleTypes[newIndex];

        eventDispatcher.fireEvent(new BoardTypeUpdateEvent(boardType));
    }

    /**
     * Generates a new deck based on the current board type and splits it into piles.
     */
    public void generateDeck() {
        this.deck = CardFactory.getInstance().generateDeck(boardType);
        splitDeckIntoPiles();
    }
    /**
     * Shuffles the deck and ensures that the first card is level 2 if the board type is LEVEL_TWO.
     * Resets the card piles.
     */
    public void shuffleDeck() {
        Collections.shuffle(deck);

        if (boardType == BoardType.LEVEL_TWO && !deck.isEmpty()) {
            if (deck.getFirst().getLevel() != 2) {
                for (int i = 1; i < deck.size(); i++) {
                    if (deck.get(i).getLevel() == 2) {
                        Collections.swap(deck, 0, i);
                        break;
                    }
                }
            }
        }
        this.cardPiles = null;
    }
    /**
     * Returns the current full deck.
     *
     * @return a list representing the full deck of cards.
     */
    public List<Card> getDeck() {
        return deck;
    }

    /**
     * Returns the list of cards in a specific pile.
     *
     * @param pileId the ID of the pile (0 to 3).
     * @return a list of cards in the specified pile.
     * @throws IllegalArgumentException if pileId is out of bounds.
     */
    public List<Card> getPile(int pileId) {
        if(pileId < 0 || pileId >= cardPiles.size()) throw new IllegalArgumentException("pileId must be between 0 and " + (cardPiles.size() - 1));
        return cardPiles.get(pileId);
    }

    /**
     * Checks if a player can be added to the game.
     *
     * @param player the player to check.
     * @return a JoinStatus indicating if the player can join.
     */
    public JoinResponseMessage.JoinStatus canAddPlayer(Player player) {
        if(gameState != LOBBY) return JoinResponseMessage.JoinStatus.GAME_STARTED;
        if(players.contains(player)) throw new IllegalArgumentException("Player already in the game.");
        if(players.size() >= 4) return JoinResponseMessage.JoinStatus.LOBBY_FULL;

        boolean nameTaken = players.stream().anyMatch(p -> p.getName().equalsIgnoreCase(player.getName()));
        if(nameTaken) return JoinResponseMessage.JoinStatus.NAME_TAKEN;

        return JoinResponseMessage.JoinStatus.SUCCESS;
    }

    /**
     * Adds a player to the game if possible.
     * Assigns the first available color and sets as admin if first player.
     * Fires a PlayersUpdateEvent.
     *
     * @param player the player to add.
     * @throws IllegalStateException if the player cannot be added.
     */
    public void addPlayer(Player player) {
        if(players.contains(player)) throw new IllegalArgumentException("Player already in the game.");
        JoinResponseMessage.JoinStatus canAddPlayer = canAddPlayer(player);
        if(canAddPlayer != JoinResponseMessage.JoinStatus.SUCCESS) throw new IllegalStateException("Can't add player :" + canAddPlayer);

        players.add(player);
        assignFirstAvailableColor(player);
        if (players.size() == 1) player.setAdmin(true);

        eventDispatcher.fireEvent(new PlayersUpdateEvent(getPlayersInfo(), player.getName()));
    }

    /**
     * Removes a player by username.
     *
     * @param username the username of the player to remove.
     * @throws IllegalArgumentException if the player is not found.
     */
    public void removePlayer(String username) {

        for (Player p : players) {
            if (p.getName().equals(username)) {
                removePlayer(p);
                return;
            }
        }
        throw new IllegalArgumentException("Player " + username + " not found.");
    }

    /**
     * Removes a player from the game.
     * Resets the game if not in lobby and all players leave.
     *
     * @param player the player to remove.
     * @throws IllegalArgumentException if the player is not in the game.
     */
    public void removePlayer(Player player) {
        if(!players.contains(player)) throw new IllegalArgumentException("Player " + player.getName() + " not found.");

        players.remove(player);

        if(player.isAdmin() && !players.isEmpty()) players.getFirst().setAdmin(true);

        if (!players.isEmpty()) {
            if (gameState != GameState.LOBBY) {
                Logger.logInfo("A player left during the game. Returning to lobby...");
                eventDispatcher.fireEvent(new GameResetEvent(player.getName()));
                resetToLobby();
            }
            eventDispatcher.fireEvent(new PlayersUpdateEvent(getPlayersInfo(), player.getName()));
        } else {
            Logger.logInfo("All players have left the game. Resetting game state.");
            resetCompletely();
        }
    }

    private void assignFirstAvailableColor(Player player) {
        Set<Player.Color> takenColors = new HashSet<>();
        for(Player p : players) {
            takenColors.add(p.getColor());
        }

        for(Player.Color color : Player.Color.values()) {
            if(!takenColors.contains(color)) {
                player.setColor(color);
                return;
            }
        }
        throw new IllegalStateException("No available colors.");
    }

    /**
     * Sets a new admin by username.
     *
     * @param username the username of the new admin.
     */
    public void setNewAdmin(String username) {
        for (Player p : players) {
            p.setAdmin(username.equals(p.getName()));
        }
        eventDispatcher.fireEvent(new PlayersUpdateEvent(getPlayersInfo(), username));
    }

    /**
     * Updates the readiness status of players and starts the game
     * if all players are ready and there are at least 2 players.
     */
    public void updatePlayersReady() {
        int playersReady = 0;
        for(Player p : players) {
            if(p.isReady()) playersReady++;
        }
        if(players.size() > 1 && playersReady == players.size()) {
            setGameStateAfterWait(BUILDING_SHIPS);
        } else {
            eventDispatcher.fireEvent(new PlayersUpdateEvent(getPlayersInfo(), null));
        }
    }

    /**
     * Returns a sorted list of players that are not eliminated.
     * Players are sorted by position in descending order.
     *
     * @return a sorted list of active players.
     */
    public List<Player> getSortedPlayers() {
        // Returns a sorted list of the non eliminated players.
        // First is the one furthest ahead (highest position value).
        return players.stream()
                .filter(player -> player.getState() != ELIMINATED)
                .sorted(Comparator.comparingInt(Player::getPosition).reversed())
                .collect(Collectors.toList());
    }
    /**
     * Returns the first player in the sorted active players list.
     *
     * @return the player furthest ahead.
     */
    public Player getFirstPlayer() {
        return getSortedPlayers().getFirst();
    }

    /**
     * Computes modulo in a way that correctly handles negative numbers.
     *
     * @param x the number.
     * @param m the modulus.
     * @return x mod m, always non-negative.
     */
    private int mod(int x, int m) {
        return ((x % m) + m) % m;
    }

    /**
     * Determines whether a specific position on the track is already occupied by another player.
     *
     * @param movingPlayer the player trying to move.
     * @param position     the position to check.
     * @return true if the position is occupied.
     */
    private boolean isPositionOccupied(Player movingPlayer, int position) {
        int flightTrackLength = boardType.getFlightTrackLength();
        return getSortedPlayers().stream()
                .filter(p -> p != movingPlayer)
                .anyMatch(p -> mod(p.getPosition(), flightTrackLength) == mod(position, flightTrackLength));
    }
    /**
     * Returns the current card in play.
     *
     * @return the current card.
     */
    public Card getCurrentCard() {
        return currentCard;
    }

    /**
     * Attempts to take a specific tile for a player.
     *
     * @param player the player attempting to take the tile.
     * @param id     the tile ID.
     * @return true if the tile was taken successfully.
     */
    public boolean TryTakeTile(Player player, int id) {
        if(player.getState() != BUILDING_SHIP) return false;

        synchronized (availableTiles) {
            if (!availableTiles.contains(id)) return false;
            availableTiles.remove(id);
            eventDispatcher.fireEvent(new TileTakenEvent(player.getName(), id));
            return true;
        }
    }

    /**
     * Returns a tile to the board.
     *
     * @param id the ID of the tile to return.
     * @throws IllegalStateException if the tile is already available.
     */
    public void returnTile(int id) {
        if(id < 0 || id >= 152) {
            Logger.logError("Tile id " + id + " is out of range.");
            return;
        }
        synchronized (availableTiles) {
            if (availableTiles.contains(id)) throw new IllegalStateException("Tile " + id + " was already on the board.");
            availableTiles.add(id);
            eventDispatcher.fireEvent(new TileReturnedEvent(id));
        }
    }
    /**
     * Attempts to take a card pile for the player.
     *
     * @param player the player taking the pile.
     * @param id     the pile ID.
     * @return true if the pile was successfully taken.
     */
    public boolean tryTakeCardPile(Player player, int id) {
        if(player.getState() != BUILDING_SHIP) return false;

        synchronized (availableCardPiles) {
            if (!availableCardPiles.contains(id)) return false;
            availableCardPiles.remove(id);
            eventDispatcher.fireEvent(new CardsTakenEvent(player.getName(), id));
            return true;
        }
    }
    /**
     * Returns a card pile to the available set.
     *
     * @param id the ID of the card pile.
     * @throws IllegalStateException if the pile is already available.
     */
    public void returnCardPile(int id) {
        if(id < 0 || id >= 3) {
            Logger.logError("Card pile id " + id + " is out of range.");
            return;
        }
        synchronized (availableCardPiles) {
            if (availableCardPiles.contains(id)) throw new IllegalStateException("Tile " + id + " was already on the board.");
            availableCardPiles.add(id);
            eventDispatcher.fireEvent(new CardsReturnedEvent(id));
        }
    }
    /**
     * Splits the current deck into 4 even piles.
     *
     * @throws IllegalArgumentException if the deck size is not divisible by 4.
     */
    public void splitDeckIntoPiles() {
        if (deck.size() % 4 != 0) {
            throw new IllegalArgumentException("Deck size must be divisible by 4");
        }
        this.cardPiles = List.of(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        int pileSize = deck.size() / 4;

        for (int i = 0; i < pileSize; i++) {
            for (int pile = 0; pile < 4; pile++) {
                int index = i * 4 + pile;
                cardPiles.get(pile).add(deck.get(index));
            }
        }
    }

    /**
     * Sets the position of a player.
     *
     * @param player   the player to move.
     * @param position the new position to set.
     * @throws IllegalArgumentException if position is occupied.
     */
    public void setPlayerPosition(Player player, int position) {
        if(player.getState() == ELIMINATED) throw new IllegalStateException("Player " + player.getName() + " is eliminated.");
        if(isPositionOccupied(player, position)) throw new IllegalArgumentException("Position is occupied");
        player.setPosition(position);
    }

    /**
     * Moves a player forward or backward a specified number of unoccupied spaces.
     *
     * @param player  the player to move.
     * @param steps   number of spaces to move.
     * @param forward true to move forward, false to move backward.
     */
    private void movePlayer(Player player, int steps, boolean forward) {
        int newPosition = player.getPosition();
        while(steps > 0) {
            newPosition += forward? 1 : -1;
            final int thisPosition = newPosition;
            // Only decrease steps if the space is not occupied.
            if(!isPositionOccupied(player, thisPosition)) steps--;
        }
        setPlayerPosition(player, newPosition);

        eventDispatcher.fireEvent(new PlayerPositionUpdateEvent(player.getName(), newPosition));
    }

    /**
     * Moves a player forward by a certain number of steps.
     * Skips over occupied spaces.
     *
     * @param player the player to move.
     * @param steps  number of steps to move forward.
     */
    public void movePlayerForward(Player player, int steps) {
        movePlayer(player, steps, true);
    }
    /**
     * Moves a player backward by a certain number of steps.
     * Skips over occupied spaces.
     *
     * @param player the player to move.
     * @param steps  number of steps to move backward.
     */
    public void movePlayerBackward(Player player, int steps) {
        movePlayer(player, steps, false);
    }

    /**
     * Computes the final score for a player.
     *
     * @param player          the player whose score is being calculated.
     * @param bestShipPlayers the set of players considered to have the best ships.
     * @return a PlayerScores object with the calculated scores.
     */
    public PlayerScores getPlayerScores(Player player, Set<Player> bestShipPlayers) {
        boolean isEliminated = player.getState() == ELIMINATED;
        boolean hasBestShip = bestShipPlayers.contains(player);

        int positionScore = isEliminated? 0 : boardType.getPositionReward(getSortedPlayers().indexOf(player) + 1);
        int bestShipScore = (isEliminated || !hasBestShip) ? 0 : boardType.getBestShipReward();
        int cargoScore = isEliminated? (player.getTotalHoldsValue() + 1) / 2 : player.getTotalHoldsValue();
        int lostTilesScore = -player.getRemovedTilesCount();

        int totaleScore = positionScore + bestShipScore + cargoScore + lostTilesScore;

        return new PlayerScores(player.getName(), positionScore, bestShipScore, cargoScore, lostTilesScore, totaleScore);
    }

    /**
     * Returns the set of players with the least number of exposed connectors.
     *
     * @return a set of players with the best ships.
     */
    public Set<Player> getPlayersWithBestShip() {
        Set<Player> playersWithBestShip = new HashSet<>();
        int minExposed = Integer.MAX_VALUE;

        for (Player player : players) {
            if(player.getState() == ELIMINATED) continue;
            int exposed = player.getExposedConnectorsCount();

            if (exposed < minExposed) {
                minExposed = exposed;
                playersWithBestShip.clear();
                playersWithBestShip.add(player);
            } else if (exposed == minExposed) {
                playersWithBestShip.add(player);
            }
        }

        return playersWithBestShip;
    }

    /**
     * Updates player statuses and eliminates players that have given up,
     * have no crew, or are too far behind.
     */
    public void updatePlayersStatus() {
        if(getSortedPlayers().isEmpty()) return;

        int highestPosition = getSortedPlayers().getFirst().getPosition();
        for(Player player : players) {
            if(player.getState() == ELIMINATED) continue;

            boolean givingUp = player.getState() == GIVING_UP;
            boolean noCrew = player.getHumanCount() == 0;
            boolean lapped = player.getPosition() < highestPosition - boardType.getFlightTrackLength();

            if(givingUp || noCrew || lapped) player.setState(ELIMINATED);
        }
    }

    private void setGameState(GameState state) {
        GameState previousGameState = gameState;
        this.gameState = state;

        eventDispatcher.fireEvent(new GameStateUpdateEvent(previousGameState, state));

        handleNewGameState(state);
    }

    private void handleNewGameState(GameState gameState) {
        switch(gameState) {
            case BUILDING_SHIPS -> {
                timerFlipAvailable = true;
                flipTimer(false);
            }
            case CORRECTING_SHIP -> {
                players.forEach(Player::applyReservedTilePenalty);

                invalidShips.clear();
                for (Player player : players) {
                    player.updateConnectivity();
                    eventDispatcher.fireEvent(new ShipUpdateEvent(player.getName(), player.getSimplifiedShipBoard()));

                    Set<Point> invalidTiles = player.getInvalidTiles();
                    if (!invalidTiles.isEmpty()) invalidShips.add(player);
                    eventDispatcher.fireEvent(new InvalidShipEvent(player.getName(), invalidTiles));
                }
                if (invalidShips.isEmpty()) setGameState(ASSIGNING_CREW);
            }
            case ASSIGNING_CREW -> {
                updatePlayersStatus();
                choosingCrewType.clear();
                for(Player player : getSortedPlayers()) {
                    eventDispatcher.fireEvent(new CrewUpdateEvent(player.getName(), player.getShipContent().crewData()));
                    Set<Point> alienCabins = player.getCabinTilesThatCanHostAliens();
                    if(!alienCabins.isEmpty()) choosingCrewType.add(player);
                    eventDispatcher.fireEvent(new AlienCabinTilesEvent(player.getName(), alienCabins));
                }
                if(choosingCrewType.isEmpty()) setGameState(PICKING_CARDS);
            }
        }
    }

    private void setGameStateAfterWait(GameState nextState) {
        setGameState(WAITING);
        if (Objects.requireNonNull(nextState) == GameState.BUILDING_SHIPS) {
            generateDeck();
            for (Player player : players) {
                player.setState(BUILDING_SHIP);
                player.setNewShipBoard(boardType);
            }
            for (int i = 1; i <= players.size(); i++) {
                availablePositions.add(i);
            }
        }
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            if(gameState == WAITING) setGameState(nextState);
            scheduler.shutdown();
        }, 5, TimeUnit.SECONDS);
    }

    /**
     * Flips the building timer if allowed. If the player hasn't finished building and there are
     * still flips available, starts the timer. Otherwise, does nothing.
     * <p>
     * This method is synchronized to prevent race conditions during timer manipulation.
     *
     * @param hasPlayerFinished indicates whether the player has finished building.
     */
    public void flipTimer(boolean hasPlayerFinished) {
        synchronized (timerHandler) {
            if(!timerFlipAvailable) return;
            if(!hasPlayerFinished && timerHandler.getTimesFlipped() >= boardType.getBuildingTimersCount() - 1) return;
            timerFlipAvailable = false;
            timerHandler.start();
        }
    }

    /**
     * Internal method invoked when the timer has finished.
     * <p>
     * If additional flips are still available, enables another flip and dispatches a partial
     * {@link TimerFinishedEvent}. Otherwise, assigns remaining positions randomly to players
     * who are still building and transitions the game to the CORRECTING_SHIP phase.
     * <p>
     * This method is synchronized to ensure correct handling of timer state and player updates.
     */
    private void onTimerFinished() {
        synchronized (timerHandler) {
            if(timerHandler.getTimesFlipped() < boardType.getBuildingTimersCount()) {
                timerFlipAvailable = true;
                eventDispatcher.fireEvent(new TimerFinishedEvent(false));
            } else {
                if (!availablePositions.isEmpty()) {

                    List<Player> stillBuilding = players.stream()
                            .filter(p -> p.getState() == Player.PlayerState.BUILDING_SHIP).toList();

                    List<Integer> remainingPositions = new ArrayList<>(availablePositions);
                    Collections.shuffle(remainingPositions);

                    for (int i = 0; i < stillBuilding.size() && i < remainingPositions.size(); i++) {
                        Player player = stillBuilding.get(i);
                        int positionIndex = remainingPositions.get(i);

                        player.setState(DONE_BUILDING);
                        player.setPosition(boardType.getStartingPosition(positionIndex));
                        availablePositions.remove(positionIndex);

                        eventDispatcher.fireEvent(new DoneBuildingEvent(player.getName(), positionIndex));
                    }
                }
                eventDispatcher.fireEvent(new TimerFinishedEvent(true));
                setGameStateAfterWait(CORRECTING_SHIP);
            }
        }
    }

    /**
     * Allows a player to claim a position once they are done building their ship.
     * Updates their state and removes the position from the available pool.
     * If all positions are claimed, progresses the game to the correcting phase.
     *
     * @param player        the player claiming a position.
     * @param positionIndex the index of the position to claim.
     */
    public void claimPosition(Player player, int positionIndex) {
        if(player.getState() != BUILDING_SHIP) return;
        synchronized (availablePositions) {
            if(!availablePositions.contains(positionIndex)) return;

            player.setState(DONE_BUILDING);
            player.setPosition(boardType.getStartingPosition(positionIndex));
            availablePositions.remove(positionIndex);

            eventDispatcher.fireEvent(new DoneBuildingEvent(player.getName(), positionIndex));
            if(availablePositions.isEmpty()) {
                timerHandler.reset();
                setGameStateAfterWait(CORRECTING_SHIP);
            }
        }
    }

    /**
     * Removes a selected tile from the player's ship board during the correcting phase.
     * If all invalid tiles are corrected for all players, advances to the crew assignment phase.
     *
     * @param player the player making the correction.
     * @param x      the x-coordinate of the tile to remove.
     * @param y      the y-coordinate of the tile to remove.
     */
    public void removeSelectedTile(Player player, int x, int y) {
        if(!invalidShips.contains(player)) {
            Logger.logWarning("Player " + player.getName() + " doesn't have an invalid ship board.");
            return;
        }
        player.removeTile(x, y);

        Set<Point> invalidTiles = player.getInvalidTiles();
        eventDispatcher.fireEvent(new InvalidShipEvent(player.getName(), invalidTiles));

        if (invalidTiles.isEmpty()) invalidShips.remove(player);

        if (invalidShips.isEmpty()) setGameState(ASSIGNING_CREW);
    }

    /**
     * Cycles the crew type at a specified tile location for a player during the crew assignment phase.
     *
     * @param player the player assigning crew.
     * @param x      the x-coordinate of the tile.
     * @param y      the y-coordinate of the tile.
     */
    public void cycleCrewTypeAt(Player player, int x, int y) {
        if(!choosingCrewType.contains(player)) {
            Logger.logWarning("Player " + player.getName() + " isn't choosing crew type.");
            return;
        }
        player.cycleCrewTypeAt(x, y);
    }

    /**
     * Marks a player as done choosing crew. If all players have finished, transitions to card picking phase.
     *
     * @param player the player that finished choosing crew.
     */
    public void doneChoosingCrew(Player player) {
        if(!choosingCrewType.contains(player)) {
            Logger.logWarning("Player " + player.getName() + " isn't choosing crew type.");
            return;
        }
        choosingCrewType.remove(player);
        eventDispatcher.fireEvent(new AlienCabinTilesEvent(player.getName(), new HashSet<>()));
        if (choosingCrewType.isEmpty()) setGameState(PICKING_CARDS);
    }

    /**
     * Triggers the next card to be picked by the current first player.
     * Validates the current game state and ensures the previous card (if any) is finished.
     *
     * @param player the player attempting to pick the next card.
     */
    public void pickNextCard(Player player) {
        if(currentCard != null && !currentCard.isFinished()) {
            Logger.logWarning("Cannot pick new card, previous card wasn't finished."); return;
        }
        if(!player.equals(getFirstPlayer())) {
            Logger.logWarning("Player " + player.getName() + " cannot pick a card, they are not first."); return;
        }
        if(deck.isEmpty()) {
            handleGameOver();
            return;
        }
        currentCard = deck.getFirst();
        deck.removeFirst();
        eventDispatcher.fireEvent(new CardPickedEvent(currentCard));

        currentCard.activate(this);
    }

    /**
     * Finalizes the game, computes the final scores for all players,
     * and dispatches a GameOverEvent with the results.
     */
    public void handleGameOver() {
        Set<Player> bestShipPlayers = getPlayersWithBestShip();
        List<PlayerScores> playersScores = players.stream()
                .map(p -> getPlayerScores(p, bestShipPlayers))
                .sorted(Comparator.comparingInt(PlayerScores::getFinalScore).reversed())
                .toList();
        resetToLobby();
        eventDispatcher.fireEvent(new GameOverEvent(playersScores));
    }

    /**
     * Resets the game state back to the lobby while preserving player information.
     * Clears decks, available tiles, and resets timers.
     */
    public void resetToLobby() {
        this.gameState = GameState.LOBBY;
        for (Player player : players) player.setState(IDLE);
        this.deck.clear();
        this.cardPiles = null;
        currentCard = null;
        this.availableTiles.clear();
        for (int i = 0; i < 152; i++) availableTiles.add(i);
        this.availableCardPiles.clear();
        for (int i = 0; i < 3; i++) availableCardPiles.add(i);
        this.timerFlipAvailable = false;
        timerHandler.reset();
    }

    /**
     * Completely resets the game, removing all players and returning
     * to the default board type and lobby state.
     */
    public void resetCompletely() {
        resetToLobby();
        this.players.clear();
        this.boardType = BoardType.LEVEL_TWO;
    }

    /**
     * Returns information about all players currently in the game.
     *
     * @return a list of {@link PlayerInfo} objects representing each player's status.
     */
    public List<PlayerInfo> getPlayersInfo(){
        return players.stream()
                .map(p -> new PlayerInfo(p.getName(),p.getProfilePictureIds(),p.getColor().toString(),p.isAdmin(),p.isReady()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the {@link EventDispatcher} used to manage and dispatch game events.
     *
     * @return the event dispatcher.
     */
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }
    /**
     * Registers a new event listener for the specified event type.
     *
     * @param <T>       the type of event.
     * @param eventType the class of the event to listen for.
     * @param listener  the listener to register.
     */
    public <T extends EventModel> void addEventListener(Class<T> eventType, ModelEventListener<T> listener) {
        eventDispatcher.addListener(eventType, listener);
    }
}
