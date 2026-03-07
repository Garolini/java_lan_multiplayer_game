package io.github.java_lan_multiplayer.server.controller.listeners;

import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.SimpleMessage;
import io.github.java_lan_multiplayer.common.messages.SimplePlayerMessage;
import io.github.java_lan_multiplayer.common.messages.flight.*;
import io.github.java_lan_multiplayer.server.events.flight.CreditsUpdateEvent;
import io.github.java_lan_multiplayer.server.events.flight.PlayerEliminatedEvent;
import io.github.java_lan_multiplayer.server.events.flight.PlayerPositionUpdateEvent;
import io.github.java_lan_multiplayer.server.events.flight.cards.*;
import io.github.java_lan_multiplayer.common.messages.flight.cardActions.CardDecisionMessage;
import io.github.java_lan_multiplayer.common.messages.flight.cardActions.CargoSwapMessage;
import io.github.java_lan_multiplayer.common.messages.flight.cardActions.CargoUnloadMessage;
import io.github.java_lan_multiplayer.server.controller.MessageDispatcher;
import io.github.java_lan_multiplayer.server.model.GameModel;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.ShipBoard;
import io.github.java_lan_multiplayer.server.model.cards.Card;
import io.github.java_lan_multiplayer.server.model.cards.projectile.CannonShot;
import io.github.java_lan_multiplayer.server.model.cards.projectile.Meteor;
import io.github.java_lan_multiplayer.server.model.cards.projectile.Projectile;
import io.github.java_lan_multiplayer.server.model.tiles.CannonTile;
import io.github.java_lan_multiplayer.server.model.tiles.CrewType;
import io.github.java_lan_multiplayer.server.model.tiles.EngineTile;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CardsListener extends EventHandler {

    public CardsListener(GameModel gameModel, MessageDispatcher dispatcher) {
        super(gameModel, dispatcher);
    }

    @Handles(PlayerPositionUpdateEvent.class)
    private void handlePlayerPositionUpdate(PlayerPositionUpdateEvent event) {
        Logger.logDebug(event.getPlayerName() + " moved to " + event.getNewPosition());
        dispatcher.broadcast(new PlayerPositionMessage(event.getPlayerName(), event.getNewPosition()));
    }

    @Handles(PlayerEliminatedEvent.class)
    private void handlePlayerEliminated(PlayerEliminatedEvent event) {
        dispatcher.broadcast(new SimplePlayerMessage("player_eliminated", event.getPlayerName()));
    }

    @Handles(CreditsUpdateEvent.class)
    private void handleCreditsUpdate(CreditsUpdateEvent event) {
        dispatcher.broadcast(new CreditsMessage(event.getPlayerName(), event.getCredits()));
    }

    @Handles(CardPickedEvent.class)
    private void handleCardPicked(CardPickedEvent event) {
        Card card = event.getCard();
        Logger.logDebug("Card picked: " + card.getCardType() + " of level " + card.getLevel());
        dispatcher.broadcast(new CardPickedMessage(card.getLevel(), card.getId()));
    }

    @Handles(CardFinishedEvent.class)
    private void handleCardFinished(CardFinishedEvent event) {
        if(gameModel.getSortedPlayers().isEmpty() || gameModel.getDeck().isEmpty()) {
            gameModel.handleGameOver();
        } else {
            dispatcher.broadcast(new PickAvailableMessage(gameModel.getFirstPlayer().getName()));
        }
    }

    @Handles(PlanetSelectionEvent.class)
    private void handlePlanetSelection(PlanetSelectionEvent event) {
        List<String> planetChosenByNames = Arrays.stream(event.getPlanetChosenBy())
                .map(p -> p != null ? p.getName() : null).toList();
        dispatcher.broadcast(new PlanetSelectionMessage(event.getPlayerName(), planetChosenByNames));
    }

    @Handles(CargoDecisionEvent.class)
    private void handleCargoDecision(CargoDecisionEvent event) {
        for(Player player : event.getPlayers()) {
            dispatcher.broadcast(new CargoDecisionMessage(player.getName(), player.getShipContent().cargoData(), player.getCargoPool()));
        }
    }

    @Handles(CargoSwappedEvent.class)
    private void handleCargoSwapped(CargoSwappedEvent event) {
        String playerName = event.getPlayer().getName();
        dispatcher.sendTo(playerName, new CargoSwapMessage(playerName, event.getBlockIndex(), event.getTileCoords(), event.getContainerIndex()));
        dispatcher.broadcastExcept(playerName, new CargoMessage(playerName, event.getPlayer().getShipContent().cargoData()));
    }

    @Handles(CargoUnloadedEvent.class)
    private void handleCargoUnloaded(CargoUnloadedEvent event) {
        String playerName = event.getPlayer().getName();
        dispatcher.sendTo(playerName, new CargoUnloadMessage(playerName, event.getTileCoords(), event.getContainerIndex()));
        dispatcher.broadcastExcept(playerName, new CargoMessage(playerName, event.getPlayer().getShipContent().cargoData()));
    }

    @Handles(CardDecisionEvent.class)
    private void handleCardDecision(CardDecisionEvent event) {
        dispatcher.broadcast(new CardDecisionMessage(event.getPlayerName(), null));
    }

    @Handles(CrewDecisionEvent.class)
    private void handleCrewDecision(CrewDecisionEvent event) {
        dispatcher.broadcast(new CrewMessage("crew_decision", event.getPlayerName(), event.getCrewTiles()));
    }

    @Handles(DecisionEndEvent.class)
    private void handleDecisionEnd(DecisionEndEvent event) {
        dispatcher.sendTo(event.getPlayerName(), new SimpleMessage("decision_end"));
    }

    @Handles(ProjectileEvent.class)
    private void handleProjectile(ProjectileEvent e) {
        Player player = e.getPlayer();
        Projectile projectile = e.getProjectile();
        ProjectileMessage.ProjectileType type = null;
        if(projectile instanceof Meteor) type = ProjectileMessage.ProjectileType.METEOR;
        if(projectile instanceof CannonShot) type = ProjectileMessage.ProjectileType.SHOT;
        if(type == null) throw new IllegalStateException("Projectile type not found.");
        Set<TileInfo> ship = (!e.isDefended() && e.getHitPoint() != null)? player.getSimplifiedShipBoard() : null;

        dispatcher.broadcast(new ProjectileMessage(player.getName(), type, projectile.getSize(), projectile.getSource(), projectile.getPathIndex(), e.getHitPoint(), e.isDefended(), ship));
    }

    @Handles(DiceThrowEvent.class)
    private void handleDiceThrow(DiceThrowEvent event) {
        if(event.getDice() == null) return;
        dispatcher.broadcast(new DiceThrowMessage(event.getPlayerName(), event.getDice()));
    }

    @Handles(BatteryDecisionEvent.class)
    private void handleBatteryDecision(BatteryDecisionEvent event) {
        Player player = event.getPlayer();
        Projectile projectile = event.getProjectile();
        Point hitPoint = player.getFirstNonEmptyTilePosition(projectile.getSource(), projectile.getPathIndex());
        dispatcher.broadcast(new BatteryDecisionMessage(player.getName(), player.getShipContent().batteriesData(), hitPoint));
    }

    @Handles(CannonSelectionEvent.class)
    private void handleCannonSelection(CannonSelectionEvent event) {
        Player player = event.getPlayer();
        Set<Point> verticalCannons = player.getDoubleCannonsPositions(true);
        Set<Point> rotatedCannons = player.getDoubleCannonsPositions(false);
        Set<ShipBoard.BatteriesData> batteries = player.getShipContent().batteriesData();
        double minStrength = player.getMinStrength(CannonTile.class);
        boolean hasAlien = player.hasAlien(CrewType.PURPLE_ALIEN);
        dispatcher.broadcast(new TileSelectionMessage("cannon_selection", player.getName(), minStrength, verticalCannons, rotatedCannons, batteries, false, hasAlien));
    }

    @Handles(EngineSelectionEvent.class)
    private void handleEngineSelection(EngineSelectionEvent event) {
        Player player = event.getPlayer();
        Set<Point> engines = player.getDoubleEnginesPositions();
        Set<ShipBoard.BatteriesData> batteries = player.getShipContent().batteriesData();
        double minStrength = player.getMinStrength(EngineTile.class);
        boolean hasAlien = player.hasAlien(CrewType.BROWN_ALIEN);
        dispatcher.broadcast(new TileSelectionMessage("engine_selection", player.getName(), minStrength, engines, null, batteries, event.isOpenSpace(), hasAlien));
    }

    @Handles(PlayerStatusEvent.class)
    private void handlePlayerStatus(PlayerStatusEvent event) {
        dispatcher.broadcast(new StatusUpdateMessage("player_status", event.getStatus().toString().toLowerCase(), event.getPlayerName()));
    }

    @Handles(CardStatusEvent.class)
    private void handleCardStatus(CardStatusEvent event) {
        dispatcher.broadcast(new StatusUpdateMessage("card_status", event.getStatus().toString().toLowerCase(), null));
    }

    @Handles(PlayerScoreEvent.class)
    private void handlePlayerScore(PlayerScoreEvent event) {
        dispatcher.broadcast(new PlayerScoreMessage(event.getPlayerName(), event.getScoreType(), event.getScore()));
    }
}
