package io.github.java_lan_multiplayer.server.controller.listeners;

import io.github.java_lan_multiplayer.common.Logger;
import io.github.java_lan_multiplayer.server.controller.MessageDispatcher;
import io.github.java_lan_multiplayer.server.events.EventModel;
import io.github.java_lan_multiplayer.server.model.GameModel;

import java.lang.reflect.Method;

public class EventScanner {

    /**
     * Registers all methods in the given handler object that are annotated with {@link Handles}
     * as event listeners for the specified {@link GameModel}.
     * <p>
     * This method scans the declared methods of the handler class for the {@code @Handles} annotation.
     * For each such method, it registers a listener with the {@code gameModel} that will invoke
     * the method via reflection when the specified event type is dispatched.
     * <p>
     * Any exceptions during invocation are caught and logged using {@code Logger.logError}.
     *
     * @param gameModel  the game model to register listeners with
     * @param handler    the object containing methods annotated with {@code @Handles}
     * @param dispatcher the message dispatcher (not used directly but assumed to be part of integration)
     */
    public static void registerHandlers(GameModel gameModel, Object handler, MessageDispatcher dispatcher) {
        for (Method method : handler.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Handles.class)) {
                Handles annotation = method.getAnnotation(Handles.class);
                Class<? extends EventModel> eventType = annotation.value();

                // Use a lambda to call the method via reflection
                gameModel.addEventListener(eventType, event -> {
                    try {
                        method.setAccessible(true);
                        method.invoke(handler, event);
                    } catch (Exception e) {
                        Logger.logError(e.getMessage());
                    }
                });
            }
        }
    }
}