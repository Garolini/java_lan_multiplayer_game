package io.github.java_lan_multiplayer.server.model.cards.projectile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.server.model.Player;

import static io.github.java_lan_multiplayer.server.model.cards.projectile.Projectile.Size.SMALL;

/**
 * Represents a cannon shot projectile fired at a player's ship.
 * Cannon shots can be either SMALL or LARGE and originate from a specified direction.
 * This projectile can be blocked with shields if it's SMALL, otherwise it damages the ship.
 */
public class CannonShot extends Projectile {

    /**
     * Constructs a new CannonShot with the given size and source direction.
     * Used in regular gameplay when the path index is determined dynamically.
     *
     * @param size   the size of the cannon shot (SMALL or LARGE)
     * @param source the direction from which the shot is fired
     */
    public CannonShot(@JsonProperty("size") Size size, @JsonProperty("direction") Source source) {
        super(size, source);
    }
    /**
     * Constructs a CannonShot with a specific path index.
     * Useful for testing or scripted scenarios.
     *
     * @param size      the size of the shot
     * @param source    the direction from which it's fired
     * @param pathIndex the fixed path index (bypassing dice roll)
     */
    public CannonShot(Size size, Source source, int pathIndex) {
        super(size, source, pathIndex);
    }

    /**
     * Evaluates whether the player successfully blocks or suffers damage from the cannon shot.
     * <ul>
     *     <li>If the targeted path is empty, no damage is applied and no defense is needed.</li>
     *     <li>If the shot is SMALL and the side is shielded, it's successfully blocked.</li>
     *     <li>Otherwise, the shot damages the ship via {@code player.applyProjectile()}.</li>
     * </ul>
     *
     * @param player the player whose ship is being targeted
     * @return {@code true} if the projectile can be blocked (via shield), {@code false} if it misses or can't be blocked
     */
    @Override
    public boolean evaluateDefense(Player player) {

        if(player.isPathEmpty(this)) return false;

        if(getSize() == SMALL && player.isSideShielded(getSource().toInt())) return true;

        player.applyProjectile(this);
        return false;
    }
}
