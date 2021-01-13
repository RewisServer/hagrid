package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import java.util.List;

/**
 * @author Tobias Büser
 */
public interface HagridListenerRegistry {

    <T> void executeListeners(String topic, Direction direction, HagridPacket<T> packet);

    boolean hasListener(String topic, Class<?> payloadClass);

    void registerListener(HagridListener listener);

    void registerListeners(Object containingInstance);

    default void registerManyListeners(Object... containingInstances) {
        for (Object containingInstance : containingInstances) {
            this.registerListeners(containingInstance);
        }
    }

    void unregisterListener(HagridListener listener);

    void unregisterListener(String topic, Class<?> payloadClass);

    void unregisterListener(String topic);

    List<HagridListener> getListener(String topic, Class<?> payloadClass);

    List<HagridListener> getListener(String topic);

}
