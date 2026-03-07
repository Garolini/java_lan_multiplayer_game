package io.github.java_lan_multiplayer.common.messages.lobby;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage implements GameMessage {

    private String sender;
    private String color;
    private String message;

    public ChatMessage() {}

    public ChatMessage(String message) {
        this.message = message;
        this.sender = null;
    }

    public ChatMessage(String sender, String color, String message) {
        this.sender = sender;
        this.color = color;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }
    public String getColor() {
        return color;
    }
    public String getMessage() {
        return message;
    }

    @Override
    public String getType() {
        return "chat_message";
    }
}
