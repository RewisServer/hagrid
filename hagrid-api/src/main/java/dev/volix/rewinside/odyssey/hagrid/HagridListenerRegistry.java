package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public interface HagridListenerRegistry {

    boolean hasListener(String topic);

    void registerListener();

    void unregisterListener();

    void getListener();

    void await(String topic);

}
