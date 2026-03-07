package io.github.java_lan_multiplayer.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleMessage implements GameMessage {

    private final String type;

    public SimpleMessage(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }
}