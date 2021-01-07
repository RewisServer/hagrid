package dev.volix.rewinside.odyssey.hagrid;

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
    public byte[] serialize(Message payload) {
        return payload.toByteArray();
    }

    @Override
    public Message deserialize(String typeUrl, byte[] data) {
        try {
            Class<? extends Message> clazz = (Class<? extends Message>) Class.forName(typeUrl);
            Message defaultInstance = Internal.getDefaultInstance(clazz);

            return defaultInstance.getParserForType().parseFrom(data);
        } catch (InvalidProtocolBufferException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
