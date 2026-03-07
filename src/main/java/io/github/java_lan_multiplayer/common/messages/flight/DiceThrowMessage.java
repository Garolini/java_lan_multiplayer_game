package io.github.java_lan_multiplayer.common.messages.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiceThrowMessage implements GameMessage {

    private String playerName;
    private int[] dice;

    public DiceThrowMessage() {}

    public DiceThrowMessage(String playerName, int[] dice) {
        this.playerName = playerName;
        this.dice = dice;
    }

    public String getPlayerName() {
        return playerName;
    }
    public int[] getDice() {
        return dice;
    }

    @Override
    public String getType() {
        return "dice_throw";
    }
}
