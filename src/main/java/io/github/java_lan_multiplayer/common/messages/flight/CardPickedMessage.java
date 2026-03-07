package io.github.java_lan_multiplayer.common.messages.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardPickedMessage implements GameMessage {

    private int cardLevel;
    private int cardId;

    public CardPickedMessage() {}

    public CardPickedMessage(int cardLevel, int cardId) {
        this.cardLevel = cardLevel;
        this.cardId = cardId;
    }

    public int getCardLevel() {
        return cardLevel;
    }
    public int getCardId() {
        return cardId;
    }

    @Override
    public String getType() {
        return "card_picked";
    }
}
