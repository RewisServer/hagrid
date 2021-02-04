package dev.volix.rewinside.odyssey.hagrid.serdes;

/**
 * A serdes is a serialization and deserialization instance.
 *
 * @author Tobias Büser
 */
public interface HagridSerdes<T> {

    /**
     * @return The class of the type this serdes is handling.
     */
    Class<T> getType();

    /**
     * Serializes the payload to a byte array, so that the bytes
     * can be send across the network and later on be put together again.
     *
     * @param payload The payload to serialize.
     *
     * @return The byte array
     */
    byte[] serialize(T payload);

    /**
     * Deserializes the given {@code data} back to the type class.
     *
     * @param typeUrl The Java url of the class i.e. {@link Class#getTypeName()}
     * @param data    The data to be deserialized
     *
     * @return The instance deserialized out of the {@code data}. Can be null
     */
    T deserialize(String typeUrl, byte[] data);

}
