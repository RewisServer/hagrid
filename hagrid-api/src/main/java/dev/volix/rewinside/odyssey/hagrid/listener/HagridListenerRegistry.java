package dev.volix.rewinside.odyssey.hagrid.listener;

import java.util.List;

/**
 * @author Tobias Büser
 */
public interface HagridListenerRegistry {

    <T> void executeListeners(String topic, Direction direction, HagridContext context, T payload);

    <T> boolean hasListener(String topic, Class<T> payloadClass);

    <T> void registerListener(HagridListener<T> listener);

    void registerListeners(Object containingInstance);

    <T> void unregisterListener(String topic, Class<T> payloadClass);

    void unregisterListener(String topic);

    List<HagridListener<?>> getListener(String topic, Class<?> payloadClass);

    List<HagridListener<?>> getListener(String topic);

}
