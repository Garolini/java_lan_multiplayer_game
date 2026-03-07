package io.github.java_lan_multiplayer.server.model;

import io.github.java_lan_multiplayer.server.events.EventDispatcher;
import io.github.java_lan_multiplayer.server.events.shipBuilding.TimerStartEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles a countdown timer that triggers a specific action after a delay.
 * Used for managing timed events during the building phase of the game.
 */
public class BuildingTimerHandler {
    private ScheduledExecutorService executor;
    private final int delaySeconds;
    private final EventDispatcher dispatcher;
    private final Runnable onFinished;

    private int timesFlipped = 0;

    /**
     * Constructs a new BuildingTimerHandler.
     *
     * @param delaySeconds the delay duration in seconds before executing the task
     * @param dispatcher an event dispatcher to notify listeners when the timer starts
     * @param onFinished a callback to execute once the timer expires
     */
    public BuildingTimerHandler(int delaySeconds, EventDispatcher dispatcher, Runnable onFinished) {
        this.delaySeconds = delaySeconds;
        this.dispatcher = dispatcher;
        this.onFinished = onFinished;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Starts the timer, increments the flip count, and dispatches the TimerStartEvent.
     * Once the delay elapses, the provided onFinished callback is executed.
     */
    public void start() {
        timesFlipped++;
        if (dispatcher != null)
            dispatcher.fireEvent(new TimerStartEvent(delaySeconds, timesFlipped));

        executor.schedule(() -> {
            if (onFinished != null) onFinished.run();
        }, delaySeconds, TimeUnit.SECONDS);
    }

    /**
     * @return the number of times the timer has been started (flipped)
     */
    public int getTimesFlipped() {
        return timesFlipped;
    }

    /**
     * Resets the timer by shutting down the current executor and creating a new one.
     * Also resets the flip counter to zero.
     */
    public void reset() {
        timesFlipped = 0;
        executor.shutdownNow();
        executor = Executors.newSingleThreadScheduledExecutor();
    }
}