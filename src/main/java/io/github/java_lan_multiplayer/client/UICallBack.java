package io.github.java_lan_multiplayer.client;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Functional interface for handling UI-originated string commands, typically in JSON format.
 * <p>
 * Implementations of this interface are responsible for parsing and processing commands from the UI layer.
 */
public interface UICallBack {

    /**
     * Processes a command string received from the UI, typically in JSON format.
     *
     * @param cmd the command string to process
     * @throws JsonProcessingException if the input command string is not valid JSON
     */
    void process(String cmd) throws JsonProcessingException;
}
