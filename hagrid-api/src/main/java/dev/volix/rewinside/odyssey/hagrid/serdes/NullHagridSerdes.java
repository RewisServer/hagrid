package dev.volix.rewinside.odyssey.hagrid.serdes;

/**
 * @author Tobias Büser
 */
public class NullHagridSerdes implements HagridSerdes<Object> {

    @Override
    public Class<Object> getType() {
        return Object.class;
    }

    @Override
    public byte[] serialize(final Object payload) {
        return new byte[0];
    }

    @Override
    public Object deserialize(final String typeUrl, final byte[] data) {
        return null;
    }

}
