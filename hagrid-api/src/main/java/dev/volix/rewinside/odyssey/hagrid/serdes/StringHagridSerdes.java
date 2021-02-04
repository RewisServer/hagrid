package dev.volix.rewinside.odyssey.hagrid.serdes;

import java.nio.charset.StandardCharsets;

/**
 * @author Tobias Büser
 */
public class StringHagridSerdes implements HagridSerdes<String> {

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public byte[] serialize(final String payload) {
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String deserialize(final String typeUrl, final byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

}
