package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.cards.Card;

public class CardPickedEvent extends EventModel {

    private final Card card;

    public CardPickedEvent(Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }
}

