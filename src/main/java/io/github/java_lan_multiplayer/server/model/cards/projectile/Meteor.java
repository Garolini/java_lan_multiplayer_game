package io.github.java_lan_multiplayer.server.model.cards.projectile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.java_lan_multiplayer.server.model.Player;
import io.github.java_lan_multiplayer.server.model.ShipBoard;

import static io.github.java_lan_multiplayer.server.model.cards.projectile.Projectile.Size.LARGE;
import static io.github.java_lan_multiplayer.server.model.cards.projectile.Projectile.Size.SMALL;
import static io.github.java_lan_multiplayer.server.model.ShipBoard.DefendingCannonType.DOUBLE;
import static io.github.java_lan_multiplayer.server.model.ShipBoard.DefendingCannonType.SINGLE;

/**
 * Represents a meteor projectile that targets a player's ship.
 * Meteors can be either SMALL or LARGE and originate from a specific direction (source).
 * Players must defend against them using shields or cannons depending on the meteor's size.
 */
public class Meteor extends Projectile {

    /**
     * Constructs a new Meteor with the specified size and source direction.
     * Used in typical gameplay where the path index is determined by dice.
     *
     * @param size   the size of the meteor (SMALL or LARGE)
     * @param source the direction from which the meteor is fired
     */
    public Meteor(@JsonProperty("size") Projectile.Size size, @JsonProperty("source") Projectile.Source source) {
        super(size, source);
    }
    /**
     * Constructs a Meteor with a specific path index.
     * Useful for testing or predefined scenarios.
     *
     * @param size      the size of the meteor
     * @param source    the direction from which it is fired
     * @param pathIndex the path index to be used instead of rolling dice
     */
    public Meteor(Size size, Source source, int pathIndex) {
        super(size, source, pathIndex);
    }

    /**
     * Evaluates whether the player successfully defends against this meteor.
     * <ul>
     *     <li>If the path is empty, no defense is needed and no damage is taken.</li>
     *     <li>If the meteor is SMALL:
     *         <ul>
     *             <li>If no exposed connector is hit, no effect.</li>
     *             <li>If the side is shielded, it's successfully blocked.</li>
     *         </ul>
     *     </li>
     *     <li>If the meteor is LARGE:
     *         <ul>
     *             <li>If only a single cannon defends, it fails to block.</li>
     *             <li>If a double cannon defends, it's successfully blocked.</li>
     *         </ul>
     *     </li>
     *     <li>If not blocked, the meteor damages the ship via {@code player.applyProjectile()}.</li>
     * </ul>
     *
     * @param player the player whose ship is being targeted
     * @return {@code true} if the meteor can be blocked; {@code false} if it hits or miss completely the ship
     */
    @Override
    public boolean evaluateDefense(Player player) {

        if(player.isPathEmpty(this)) return false;

        if(getSize() == SMALL) {
            if(!player.hasExposedConnectorInPath(this)) return false;

            if(player.isSideShielded(getSource().toInt())) return true;

        } else if(getSize() == LARGE) {
            ShipBoard.DefendingCannonType cannonType = player.getDefendingCannonType(this);
            if(cannonType == SINGLE) return false;
            if(cannonType == DOUBLE) return true;
        }

        player.applyProjectile(this);
        return false;
    }
}
