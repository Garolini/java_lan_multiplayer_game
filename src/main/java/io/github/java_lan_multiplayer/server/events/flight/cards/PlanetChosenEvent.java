package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;

public class PlanetChosenEvent extends EventModel {

    private final String playerName;
    private final int chosenPlanet;

    public PlanetChosenEvent(String playerName, int chosenPlanet) {
        this.playerName = playerName;
        this.chosenPlanet = chosenPlanet;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getChosenPlanet() {
        return chosenPlanet;
    }
}
