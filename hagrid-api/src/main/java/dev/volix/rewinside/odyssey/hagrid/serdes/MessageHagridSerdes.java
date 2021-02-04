package dev.volix.rewinside.odyssey.hagrid.serdes;

import com.google.protobuf.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

/**
 * @author Tobias Büser
 */
public class MessageHagridSerdes implements HagridSerdes<Message> {

    @Override
    public Class<Message> getType() {
        return Message.class;
    }

    @Override
    public byte[] serialize(final Message payload) {
        return payload.toByteArray();
    }

    @Override
    public Message deserialize(final String typeUrl, final byte[] data) {
        try {
            final Class<? extends Message> clazz = (Class<? extends Message>) Class.forName(typeUrl);
            final Message defaultInstance = Internal.getDefaultInstance(clazz);

            return defaultInstance.getParserForType().parseFrom(data);
        } catch (final InvalidProtocolBufferException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
