package io.github.java_lan_multiplayer.client.popups;

/**
 * Interface to be implemented by controllers of popup views.
 * <p>
 * Enables lifecycle management and communication between the {@link Popup} class
 * and the controller associated with the FXML file used in the popup.
 * <p>
 * When a popup is shown, it automatically invokes the appropriate methods
 * on a {@code PopupAware} controller if implemented.
 */
public interface PopupAware {

    /**
     * Binds the controller to the corresponding {@link Popup} instance.
     * This allows the controller to interact with the popup, for example to close it programmatically.
     *
     * @param popup the popup instance this controller is managing
     */
    void bindPopup(Popup popup);

    /**
     * Called when the popup is shown or hidden.
     * Useful for updating internal visibility flags or pausing background logic.
     *
     * @param visible {@code true} if the popup is currently visible, {@code false} otherwise
     */
    void setPopupVisible(boolean visible);

    /**
     * Returns whether this popup is currently visible.
     *
     * @return {@code true} if the popup is visible, {@code false} otherwise
     */
    boolean isPopupVisible();

    /**
     * Initializes the popup controller with optional arguments.
     * These arguments are provided during the popup construction process.
     *
     * @param args the optional arguments passed from the {@link Popup} factory method
     */
    void initialize(Object... args);

    /**
     * Called when the popup is being closed.
     * Use this method to perform cleanup, animation finalization, or to persist state.
     */
    void onClose();
}

