package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public interface HagridSerdes<T> {

    Class<T> getType();

    byte[] serialize(T payload);

    T deserialize(String typeUrl, byte[] data);

}
