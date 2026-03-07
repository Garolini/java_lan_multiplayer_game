package io.github.java_lan_multiplayer.common.messages.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;
import io.github.java_lan_multiplayer.server.model.cards.projectile.Projectile;

import java.awt.*;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectileMessage implements GameMessage {

    public enum ProjectileType {
        METEOR, SHOT
    }
    private String playerName;
    private ProjectileType projectileType;
    private Projectile.Size size;
    private Projectile.Source source;
    private Integer pathIndex;
    private Point hitPoint;
    private boolean defended;
    private Set<TileInfo> tiles;

    public ProjectileMessage() {}

    public ProjectileMessage(String playerName, ProjectileType projectileType, Projectile.Size size, Projectile.Source source, Integer pathIndex, Point hitPoint, boolean defended, Set<TileInfo> tiles) {
        this.playerName = playerName;
        this.projectileType = projectileType;
        this.size = size;
        this.source = source;
        this.pathIndex = pathIndex;
        this.hitPoint = hitPoint;
        this.defended = defended;
        this.tiles = tiles;
    }

    public String getPlayerName() {
        return playerName;
    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }
    public Projectile.Size getSize() {
        return size;
    }
    public Projectile.Source getSource() {
        return source;
    }
    public Integer getPathIndex() {
        return pathIndex;
    }
    public Point getHitPoint() {
        return hitPoint;
    }
    public boolean isDefended() {
        return defended;
    }
    public Set<TileInfo> getTiles() {
        return tiles;
    }

    @Override
    public String getType() {
        return "projectile";
    }
}
