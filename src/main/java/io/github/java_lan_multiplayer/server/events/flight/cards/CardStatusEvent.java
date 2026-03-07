package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class CardStatusEvent extends EventModel {

    public enum CardStatus {
        RULE_CREW, RULE_CANNON, RULE_ENGINE, COMBAT_SKIPPED, EPIDEMIC, STARDUST
    }

    private final CardStatus status;

    public CardStatusEvent(CardStatus status) {
        this.status = status;
    }

    public CardStatus getStatus() {
        return status;
    }
}
