package io.github.java_lan_multiplayer.server.events.flight.cards;

import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.cards.projectile.Projectile;

public class BatteryDecisionEvent extends EventModel {

    private final Player player;
    private final Projectile projectile;

    public BatteryDecisionEvent(Player player, Projectile projectile) {
        this.player = player;
        this.projectile = projectile;
    }

    public Player getPlayer() {
        return player;
    }

    public Projectile getProjectile() {
        return projectile;
    }
}
