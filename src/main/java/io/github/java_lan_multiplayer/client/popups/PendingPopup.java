package io.github.java_lan_multiplayer.client.popups;

/**
 * Utility class for managing a single pending {@link Popup} instance.
 * <p>
 * This class allows a popup to be scheduled and shown later via the {@code tryShow()} method.
 * It ensures only one popup is managed at a time. Once shown, the reference is cleared.
 * </p>
 *
 * <p>
 * Commonly used for deferring UI popups until the JavaFX application thread is ready,
 * such as after a scene has loaded or during controller initialization.
 * </p>
 *
 * @see Popup
 */
public class PendingPopup {

    private static Popup popup;

    /**
     * Sets the pending popup to be shown later.
     * If a popup is already pending, it will be overwritten.
     *
     * @param popup the {@link Popup} instance to store
     */
    public static void set(Popup popup) {
        PendingPopup.popup = popup;
    }

    /**
     * Attempts to show the stored popup if one exists.
     * After showing, the stored popup is cleared to avoid reuse.
     * If no popup is set, this method does nothing.
     */
    public static void tryShow() {
        if (popup != null) {
            popup.show();
            popup = null;
        }
    }

    /**
     * Clears any stored popup without showing it.
     * Useful when the pending popup is no longer needed or relevant.
     */
    public static void clear() {
        popup = null;
    }
}