package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import java.util.List;

/**
 * Represents a registry for {@link HagridListener}.
 *
 * @author Tobias Büser
 */
public interface HagridListenerRegistry {

    /**
     * Execute all listeners that match given parameters.
     *
     * @param topic     The topic of the {@code packet}. Will be checked against
     *                  the pattern of {@link HagridListener#getTopic()}
     * @param direction The direction of the {@code packet}
     * @param packet    The packet itself
     */
    <T> void executeListeners(String topic, Direction direction, HagridPacket<T> packet);

    /**
     * Checks if a listener with given parameters is already registered.
     *
     * @param topic        The topic pattern of {@link HagridListener#getTopic()}
     * @param payloadClass Class of the payload to listen on.
     *                     Can also be {@code null} or {@link Void} to represent
     *                     <b>any</b> payload type.
     *
     * @return If a listener with this parameters is already registered.
     */
    boolean hasListener(String topic, Class<?> payloadClass);

    /**
     * Registers given listener.
     *
     * @param listener The listener instance.
     */
    void registerListener(HagridListener listener);

    /**
     * Register all listeners that are containted in given instance.
     * <p>
     * Contained in this context means that it will select
     * all methods that are fit to be registered as a listener,
     * just like it is described in {@link HagridListens}.
     *
     * @param containingInstance The instance of the class.
     */
    void registerListeners(Object containingInstance);

    /**
     * @see #registerListeners(Object)
     */
    default void registerManyListeners(final Object... containingInstances) {
        for (final Object containingInstance : containingInstances) {
            this.registerListeners(containingInstance);
        }
    }

    /**
     * Unregisters a listener.
     *
     * @param listener The listener instance to be unregistered.
     */
    void unregisterListener(HagridListener listener);

    /**
     * Unregister a listener that matches given parameters.
     *
     * @param topic        The exact topic pattern of the listener.
     * @param payloadClass The payload class that the listener listens on.
     */
    void unregisterListener(String topic, Class<?> payloadClass);

    /**
     * @see #unregisterListener(String, Class)
     */
    void unregisterListener(String topic);

    /**
     * Gets all listeners that matches given parameters.
     *
     * @param topic        The topic, will be matched against the {@link HagridListener#getTopic()} pattern.
     * @param payloadClass The class of the payload to listen to.
     *
     * @return Returns a list of all found listeners or an empty list.
     */
    List<HagridListener> getListener(String topic, Class<?> payloadClass);

    /**
     * @see #getListener(String, Class)
     */
    List<HagridListener> getListener(String topic);

}
