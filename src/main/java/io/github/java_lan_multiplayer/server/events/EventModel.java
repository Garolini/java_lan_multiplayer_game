package io.github.java_lan_multiplayer.server.events;

import java.io.Serializable;

/**
 * The base class for all model events in the application.
 *
 * <p>All specific event types should extend this class. Being {@link Serializable}
 * allows these events to be sent over a network or saved if necessary.</p>
 */
public abstract class EventModel implements Serializable {
    // No fields or methods by default; extend for specific event data.
}