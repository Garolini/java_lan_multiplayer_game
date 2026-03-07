package io.github.java_lan_multiplayer.server.controller.listeners;

import io.github.java_lan_multiplayer.server.events.EventModel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Handles {
    Class<? extends EventModel> value();
}