# New Hagrid README.md

# Hagrid Java

Default Java and Kafka implementation for Hagrid.

Hagrid itself is a system to easily communicate with pub/sub based systems like Kafka and RabbitMQ. It uses Protobuf to serialize and deserialize the sent data, as it is really efficient and has a concise protocol definition syntax.
For us, we can use Hagrid to realize microservices and their communication with each other.

In this document we want to elaborate on how to use Hagrid and how to create your own implementation (e.g. if you want to use RabbitMQ).

# Quick guide

Here we want to give a shortcut example on how to quickly use Hagrid.

First we have to import the Maven repository for Hagrid itself and for the Grape service which serves the `HagridService`.

```xml
<dependency>
    <groupId>dev.volix.rewinside.odyssey.hagrid</groupId>
    <artifactId>hagrid-api</artifactId>
    <version>1.2.0</version>
</dependency>

<dependency>
    <groupId>dev.volix.lib</groupId>
    <artifactId>grape-api</artifactId>
    <version>[0.0,)</version>
    <scope>provided</scope>
</dependency>
```

Now we can access Grape and get our service by using:

```java
Future<HagridService> future = Grape.getInstance().get(HagridService.class);
HagridService hagrid = future.get();
```

After that we can now start sending packets.

```java
hagrid.communication().registerTopic("chat", new StringHagridSerdes());

hagrid.wizard().topic("chat")
    .payload("Hello there!")
    .send();
```

# Topics

Just like the name suggests, *topics* are like a channel where specific messages flow through. That means, that data get tagged with a topic such that consumers who subscribe to that topic can all consume said data.

In our example above we used the topic `chat`, which could be like a chatroom, where many receivers can consume a message.

## Patterns

In Hagrid topics are more like a *pattern* of topics and they are not specific. Which means if you listen to the topic `chat`, you also listen to all *subtopics* of chat (e.g. `chat-global`, `chat-friends`, `chat-friends-sports` and so on).

Some valid patterns are:
- `topic`: Listen to all subtopics, no matter the depth.
- `topic-*`: Listen to only subtopics with the depth of 1.
- `topic-subtopic-*`: Listen to only subtopics of `topic-subtopic` with depth 1.
- `topic-*-*`: Listen to subtopics of `topic` with the depth 2.

## Serdes

Each topic needs to have a specific serdes (serializer and deserializer) attached to it, so that Hagrid can understand how to pack and unpack the data.

There are some default serdes, that could be helpful:
- **NullHagridSerdes**: No matter what data gets sent, it gets trashed. This can be used to just test connections, ping services, etc.
- **StringHagridSerdes**: Allows sending plain text as strings.
- **MessageHagridSerdes**: Key serdes of Hagrid, which allows sending Protobuf messages across the network.

To create a custom one, you simply have to implement `HagridSerdes` like so:

```java
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
```

# Listener

Now that we concluded how topics work, we can go on explaining how to listen to packets.

There are actually two ways to create and register a listener.

## Creating the instance directly

If you want the fast way, you can use the following:

```java
hagrid.communication().registerListener(HagridListener.builder(new HagridListenerMethod<String>() {
    @Override
    public void listen(String payload, HagridPacket<String> req, HagridResponse response) {
        // do stuff with the payload
    }
}).payloadClass(String.class).topic("chat").build());
```

In the builder you can set many different things, but most important is the `payloadClass` and the `topic`. These two have to be set.

## Using annotations

For convenience you can use an annotation and then register the enclosing class of the method, so that the method gets registered as a listener.

Here is an example:

```java
public class SomeClass {

    @HagridListens(topic = "chat")
    public void onChat(String payload) {
        System.out.println("Harry says: " + payload);
    }

}
```

As you can see in the method parameter list, we can skip the last two, if we don't need them. The valid parameters are:
- `onFoo(T payload)`
- `onFoo(T payload, HagridPacket<T> req)`
- `onFoo(T payload, HagridPacket<T> req, HagridResponse res)`

Now we can register our listener by using the following:

```java
hagrid.communication().registerListeners(new SomeClass());
```

That way every method with the `@HagridListens` annotation gets registered. Note: The methods can be private or static, it does not matter.

## Overriding annotations

If you have a lot of listener methods in a single class you may want to set the topic for all of them. This can be done by annotating the enclosing class, like so:

```java
@HagridListens(topic = "chat")
public class SomeClass {

    @HagridListens
    public void onChat1(String payload) {
        System.out.println("Harry says: " + payload);
    }
    
    @HagridListens
    public void onChat2(String payload) {
        System.out.println("Harry also says: " + payload);
    }

}
```

Now all listener methods automatically have the topic set to `chat` by the class.

## Outgoing

In Hagrid we support listening to both incoming as well as outgoing packets. Outgoing means, that if we sent a packet via our instance, the outgoing listener will trigger.

```java
@HagridListens(topic = "chat", direction = Direction.UPSTREAM)
public void onChat(String payload) {
    System.out.println("We sent following message: " + payload);
}
```

Important: This listener will be executed **after** the data has been sent.

## Responsiveness

If we have a structure such that all our listeners need to respond to the request they are getting, it can get quite tedious to handle that ourselves.

For that we can use a second annotation `@HagridResponds`, to say that we want to always send a response, no matter if it fails or if we don't specify a response at all.

```java
@HagridResponds
@HagridListens(topic = "chat")
public void onChat(String payload) {
    int someNumber = Integer.parseInt(payload);
    System.out.println("We received a number: " + someNumber);
}
```

Now if the payload is a number, everything is fine and we would respond with the status **OK**, otherwise we would automatically respond with **INTERNAL** and a message specifying the exception.

## Taking advantage of topic patterns

As explained above topics on Hagrid's site can be seen as a pattern. This can become useful if we e.g. have chatrooms and we want to listen to all of them.

```java
@HagridListens(topic = "chat-room-*")
public void onChat(String payload, HagridPacket<?> req) {
    System.out.printf("Someone in room %s said: %s", req.getTopic(), payload);
}
```

You can see, that we can listen to all subtopics of `chat-room-*` with depth 1 and that we can get the actual topic from our packet via `req.getTopic()`.
