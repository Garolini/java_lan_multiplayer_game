package io.github.java_lan_multiplayer.server.controller.commandHandlers;

import io.github.java_lan_multiplayer.common.messages.SimplePlayerMessage;
import io.github.java_lan_multiplayer.common.messages.flight.ShipBoardMessage;
import io.github.java_lan_multiplayer.common.messages.shipBuilding.*;
import io.github.java_lan_multiplayer.server.controller.MessageDispatcher;
import io.github.java_lan_multiplayer.server.model.GameModel;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.VirtualView;
import io.github.java_lan_multiplayer.server.model.cards.Card;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipBuildingCmdHandler extends CommandHandler {

    public ShipBuildingCmdHandler(GameModel gameModel, Map<VirtualView, Player> playerMap, MessageDispatcher dispatcher) {
        super(gameModel, playerMap, dispatcher);
        registerHandlers();
    }

    private final Map<VirtualView, Integer> tileMap = new HashMap<>();
    private final Map<VirtualView, Integer> cardsMap = new HashMap<>();

    @Override
    protected void registerHandlers() {
        register("tile_taken", (view, cmd) -> handleTileTaken(view, mapper.convertValue(cmd, TileTakenMessage.class)));
        register("tile_returned", (view, cmd) -> handleTileReturned(view));
        register("tile_fixed", (view, cmd) -> handleTileFixed(view, mapper.convertValue(cmd, TileFixedMessage.class)));
        register("reserve_tile", (view, cmd) -> handleReserveTile(view, mapper.convertValue(cmd, ReserveTileMessage.class)));
        register("use_reserved_tile", (view, cmd) -> handleUseReservedTile(view, mapper.convertValue(cmd, UseReservedTileMessage.class)));
        register("cards_taken", (view, cmd) -> handleCardsTaken(view, mapper.convertValue(cmd, CardsTakenMessage.class)));
        register("cards_returned", (view, cmd) -> handleCardsReturned(view));
        register("timer_flipped", (view, cmd) -> handleTimerFlipped(view));
        register("done_building", (view, cmd) -> handleDoneBuilding(view, mapper.convertValue(cmd, DoneBuildingMessage.class)));
        register("ship_request", (view, cmd) -> handleShipBoardRequest(view, mapper.convertValue(cmd, SimplePlayerMessage.class)));
    }


    private void handleTileTaken(VirtualView senderView, TileTakenMessage message) {
        if(isPlayerHoldingItem(senderView)) return;

        int id = message.getTileId();
        Player player = playerMap.get(senderView);

        if(gameModel.TryTakeTile(player, id)) {
            tileMap.put(senderView, id);
        }
    }

    private void handleTileReturned(VirtualView senderView) {
        if(!tileMap.containsKey(senderView)) {
            Logger.logWarning("Player was not holding a tile");
            return;
        }
        int tileId = tileMap.get(senderView);
        tileMap.remove(senderView);

        gameModel.returnTile(tileId);
    }

    private void handleTileFixed(VirtualView senderView, TileFixedMessage message) {
        if(!tileMap.containsKey(senderView)) {
            Logger.logWarning("Player was not holding a tile");
            return;
        }

        int tileId = tileMap.get(senderView);
        int x = message.getX();
        int y = message.getY();
        int rotation = message.getRotation();

        tileMap.remove(senderView);
        playerMap.get(senderView).setTile(x, y, tileId, rotation);
    }

    private void handleReserveTile(VirtualView senderView, ReserveTileMessage message) {
        if(!tileMap.containsKey(senderView)) {
            Logger.logWarning("Player was not holding a tile");
            return;
        }

        int tileId = tileMap.get(senderView);
        int slotId = message.getSlotId();

        tileMap.remove(senderView);
        playerMap.get(senderView).reserveTile(tileId, slotId);
    }

    private void handleUseReservedTile(VirtualView senderView, UseReservedTileMessage message) {
        if(isPlayerHoldingItem(senderView)) return;


        int slotId = message.getSlotId();
        int tileId = playerMap.get(senderView).useReservedTile(slotId);

        tileMap.put(senderView, tileId);
    }

    private void handleCardsTaken(VirtualView senderView, CardsTakenMessage cardsTakenMessage) {
        if(isPlayerHoldingItem(senderView)) return;

        int id = cardsTakenMessage.getPileId();
        Player player = playerMap.get(senderView);

        if(gameModel.tryTakeCardPile(player, id)) {
            cardsMap.put(senderView, id);

            List<Card> pile = gameModel.getPile(id);
            int[] levelOneCards = pile.stream()
                    .filter(card -> card.getLevel() == 1)
                    .mapToInt(Card::getId).toArray();
            int[] levelTwoCards = pile.stream()
                    .filter(card -> card.getLevel() == 2)
                    .mapToInt(Card::getId).toArray();

            senderView.sendMessage(new CardsMessage(levelOneCards, levelTwoCards));
        }
    }

    private void handleCardsReturned(VirtualView senderView) {
        if(!cardsMap.containsKey(senderView)) {
            Logger.logWarning("Player was not holding cards");
            return;
        }
        int pileId = cardsMap.get(senderView);
        cardsMap.remove(senderView);

        gameModel.returnCardPile(pileId);
    }

    private void handleTimerFlipped(VirtualView senderView) {
        boolean hasPlayerFinishedBuilding = playerMap.get(senderView).getState() == Player.PlayerState.DONE_BUILDING;

        gameModel.flipTimer(hasPlayerFinishedBuilding);
    }

    private void handleDoneBuilding(VirtualView senderView, DoneBuildingMessage message) {
        int positionIndex = message.getPositionIndex();
        Player player = playerMap.get(senderView);

        gameModel.claimPosition(player, positionIndex);
    }

    private void handleShipBoardRequest(VirtualView senderView, SimplePlayerMessage message) {
        gameModel.getSortedPlayers().stream()
                .filter(p -> p.getName().equals(message.getPlayerName()))
                .findFirst()
                .ifPresentOrElse(
                        player -> senderView.sendMessage(new ShipBoardMessage(player.getName(), player.getSimplifiedShipBoard())),
                        () -> Logger.logWarning("Player not found: " + message.getPlayerName())
                );
    }


    private boolean isPlayerHoldingItem(VirtualView senderVirtualView) {
        if(!tileMap.containsKey(senderVirtualView) && !cardsMap.containsKey(senderVirtualView)) return false;

        Logger.logWarning("Player " + playerMap.get(senderVirtualView).getName() + " is already holding an item.");
        return true;
    }
}
