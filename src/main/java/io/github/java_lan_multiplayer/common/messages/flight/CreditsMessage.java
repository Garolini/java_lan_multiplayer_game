package io.github.java_lan_multiplayer.common.messages.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditsMessage implements GameMessage {

    private String playerName;
    private int credits;

    public CreditsMessage() {}

    public CreditsMessage(String playerName, int credits) {
        this.playerName = playerName;
        this.credits = credits;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getCredits() {
        return credits;
    }

    @Override
    public String getType() {
        return "credits";
    }
}
