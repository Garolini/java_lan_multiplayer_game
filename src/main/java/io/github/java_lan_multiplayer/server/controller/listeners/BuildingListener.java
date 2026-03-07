package io.github.java_lan_multiplayer.server.controller.listeners;

import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.common.messages.shipBuilding.*;
import io.github.java_lan_multiplayer.server.events.shipBuilding.*;
import io.github.java_lan_multiplayer.server.controller.MessageDispatcher;
import io.github.java_lan_multiplayer.server.model.GameModel;

public class BuildingListener extends EventHandler {

    public BuildingListener(GameModel gameModel, MessageDispatcher dispatcher) {
        super(gameModel, dispatcher);
    }


    @Handles(TileTakenEvent.class)
    public void handleTileTaken(TileTakenEvent event) {
        Logger.logDebug(event.getPlayerName() + " took tile: " + event.getTileId());
        dispatcher.broadcast(new TileTakenMessage(event.getPlayerName(), event.getTileId()));
    }

    @Handles(TileReturnedEvent.class)
    public void handleTileReturned(TileReturnedEvent event) {
        Logger.logDebug("Tile returned: " + event.getTileId());
        dispatcher.broadcast(new TileReturnedMessage(event.getTileId()));
    }

    @Handles(CardsTakenEvent.class)
    public void handleCardsTaken(CardsTakenEvent event) {
        Logger.logDebug(event.getPlayerName() + " took cards: " + event.getPileId());
        dispatcher.broadcast(new CardsTakenMessage(event.getPlayerName(), event.getPileId()));
    }

    @Handles(CardsReturnedEvent.class)
    public void handleCardReturned(CardsReturnedEvent event) {
        Logger.logDebug("Cards returned: " + event.getPileId());
        dispatcher.broadcast(new CardsReturnedMessage(event.getPileId()));
    }

    @Handles(TimerStartEvent.class)
    public void handleTimerStart(TimerStartEvent event) {
        Logger.logDebug("Started timer " + event.getTimesFlipped() + " of " + event.getDuration() + " seconds");
        dispatcher.broadcast(new TimerStartMessage(event.getDuration(), event.getTimesFlipped()));
    }

    @Handles(TimerFinishedEvent.class)
    public void handleTimerFinished(TimerFinishedEvent event) {
        Logger.logDebug(event.isFinalFlip()? "Final timer ended" : "Timer ended");
        dispatcher.broadcast(new TimerFinishedMessage(event.isFinalFlip()));
    }

    @Handles(DoneBuildingEvent.class)
    public void handleDoneBuilding(DoneBuildingEvent event) {
        Logger.logDebug(event.getPlayerName() + " finished building and took position " + event.getPosition());
        dispatcher.broadcast(new DoneBuildingMessage(event.getPlayerName(), event.getPosition()));
    }
}
