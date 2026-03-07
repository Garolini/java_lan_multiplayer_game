package io.github.java_lan_multiplayer.server.events;

/**
 * A generic listener interface for receiving model events.
 *
 * <p>Classes implementing this interface can register themselves to listen
 * for specific types of {@link EventModel} events and respond when such events are fired.</p>
 *
 * @param <T> the specific subclass of {@link EventModel} this listener handles
 */
public interface ModelEventListener <T extends EventModel> {

    /**
     * Called when an event of type {@code T} is fired.
     *
     * @param event the event object
     */
    void onEvent(T event);
}
