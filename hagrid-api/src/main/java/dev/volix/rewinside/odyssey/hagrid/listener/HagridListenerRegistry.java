package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import java.util.List;

/**
 * @author Tobias Büser
 */
public interface HagridListenerRegistry {

    <T> void executeListeners(String topic, Direction direction, HagridContext context, HagridPacket<T> packet);

    <T> boolean hasListener(String topic, Class<T> payloadClass);

    <T> void registerListener(HagridListener<T> listener);

    void registerListeners(Object containingInstance);

    <T> void unregisterListener(HagridListener<T> listener);

    <T> void unregisterListener(String topic, Class<T> payloadClass);

    void unregisterListener(String topic);

    List<HagridListener<?>> getListener(String topic, Class<?> payloadClass);

    List<HagridListener<?>> getListener(String topic);

}
