package io.github.java_lan_multiplayer.common.messages.shipBuilding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardsMessage implements GameMessage {

    private int[] levelOneCards;
    private int[] levelTwoCards;

    public CardsMessage() {}

    public CardsMessage(int[] levelOneCards, int[] levelTwoCards) {
        this.levelOneCards = levelOneCards;
        this.levelTwoCards = levelTwoCards;
    }

    public int[] getLevelOneCards() {
        return levelOneCards;
    }

    public int[] getLevelTwoCards() {
        return levelTwoCards;
    }

    @Override
    public String getType() {
        return "cards";
    }
}
