package io.github.java_lan_multiplayer.client;

/**
 * Interface for components that require awareness or reinitialization when the virtual server is updated.
 * <p>
 * This is commonly used for GUI components or other observers that must rebind or refresh themselves
 * when a new virtual server or context becomes available.
 */
public interface VirtualServerAware {

    /**
     * Notifies the implementing component that the virtual server context has changed,
     * and it should refresh or update its callback bindings.
     */
    void updateCallBack();
}
