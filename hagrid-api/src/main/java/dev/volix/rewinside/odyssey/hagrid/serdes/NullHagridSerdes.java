package dev.volix.rewinside.odyssey.hagrid.serdes;

/**
 * @author Tobias Büser
 */
public class NullHagridSerdes implements HagridSerdes<Void> {

    @Override
    public Class<Void> getType() {
        return Void.class;
    }

    @Override
    public byte[] serialize(final Void payload) {
        return new byte[0];
    }

    @Override
    public Void deserialize(final String typeUrl, final byte[] data) {
        return null;
    }

}
