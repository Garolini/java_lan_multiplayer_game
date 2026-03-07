package io.github.java_lan_multiplayer.server.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code EventDispatcher} class manages the registration and firing
 * of model events to appropriate listeners.
 *
 * <p>It supports multiple listeners for each event type and ensures that
 * listeners are notified when an event they subscribed to is triggered.</p>
 */
public class EventDispatcher {
    private final Map<Class<? extends EventModel>, List<ModelEventListener<? extends EventModel>>> listeners = new HashMap<>();

    /**
     * Registers a listener for a specific event type.
     *
     * @param eventType the class object representing the event type
     * @param listener  the listener to register
     * @param <T>       the type of event
     */
    public <T extends EventModel> void  addListener(Class<T> eventType, ModelEventListener<T> listener) {
        listeners.computeIfAbsent(eventType, _ -> new ArrayList<>()).add(listener);
    }

    /**
     * Fires an event, notifying all registered listeners for the event's type.
     *
     * @param event the event instance to fire
     * @param <T>   the type of the event
     */
    @SuppressWarnings("unchecked")
    public <T extends EventModel> void fireEvent(T event) {
        List<ModelEventListener<? extends EventModel>> registered = listeners.get(event.getClass());
        if (registered != null) {
            for (ModelEventListener<? extends EventModel> listener : registered) {
                ((ModelEventListener<T>) listener).onEvent(event);
            }
        }
    }
}
