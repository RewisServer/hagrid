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

## Registering

Without registering a topic, Hagrid will not subscribe to it and therefore not know when a packet goes into the topic.
Additionally, you can not send a packet into the topic, as Hagrid needs to know how to serialize it.

```java
hagrid.communication().registerTopic("chat", new StringHagridSerdes());
```

You can see that the method expects two parameters, first is the name of the topic and second is the serdes, that we want to use for this specific topic.

Important note: The topic **must not** be a topic pattern, but it will still automatically set the serdes for all subtopics as well.

## Options

If you want to have more options for the specific topic, you can use the method as follows:

```java
hagrid.communication().registerTopic("chat", new StringHagridSerdes(),
    TopicPropertes().newBuilder().receiveStalePackets(false).build())
```

That way you can configure the topic with different options. In this case the topic will not receive packets that have been sent while the service was inactive.

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

Otherwise if we want to specify a status ourselves, we can use the following:

```java
@HagridListens(topic = "chat")
public void onChat(String payload, HagridPacket<?> req, HagridResponse res) {
    try {
        res.status(StatusCode.OK, "everything is fine.");
    } catch (NumberFormatException ex) {
        res.status(StatusCode.BAD_REQUEST, "you have not sent a number");
        return;
    }
    System.out.println("We received a number: " + someNumber);
}
```

That way after the listener has been executed, it will automatically construct a needed response with the status given.
To add some kind of more detailed description of what happened, you can use something called `subcodes`.

```java
@HagridListens(topic = "chat")
public void onChat(String payload, HagridPacket<?> req, HagridResponse res) {
    try {
        res.status(StatusCode.OK, "everything is fine.");
    } catch (NumberFormatException ex) {
        // adding a subcode as an enum or an int to the status
        res.status(StatusCode.BAD_REQUEST, ChatSubCode.NOT_A_NUMBER, "you have not sent a number");
        return;
    }
    System.out.println("We received a number: " + someNumber);
}
```

In this case `ChatSubCode` is an enum created by the application, which then gets transformed into an int by the ordinal - more specifically `ordinal() + 1` as the subcode starts counting at 1.
Now the receiver of the response can also use `ChatSubCode` to get a better idea of what `BAD_REQUEST` means specifically without having to map the message to a specific status.

## Taking advantage of topic patterns

As explained above topics on Hagrid's site can be seen as a pattern. This can become useful if we e.g. have chatrooms and we want to listen to all of them.

```java
@HagridListens(topic = "chat-room-*")
public void onChat(String payload, HagridPacket<?> req) {
    System.out.printf("Someone in room %s said: %s", req.getTopic(), payload);
}
```

You can see, that we can listen to all subtopics of `chat-room-*` with depth 1 and that we can get the actual topic from our packet via `req.getTopic()`.

# Sending packets

Now that we understand how all these things work, we can finally explain a bit more on how sending packets work.

## Without expected response

Without expecting a response, we can just send the packet down the line. Just like we showed in the example at the beginning, we can send one like this:

```java
hagrid.wizard().topic("chat")
    .payload("Hello there!")
    .send();
```

If we now want to respond to another packet, we can use something like this:

```java
HagridPacket<String> question = new HagridPacket<>("How are you?");

hagrid.wizard().topic("chat")
    .respondsTo(question.getId())
    .payload("I'm fine!")
    .send();
```

Of course the packet `question` does not have an id at the moment, the id gets generated automatically when sending a packet. But you can see, that responding to an incoming packet is really easy!

Let's do the same but instead we want to say, that we are not fine:

```java
hagrid.wizard().topic("chat")
    .respondsTo(question.getId())
    .status(StatusCode.INTERNAL)
    .payload("I'm not fine ..")
    .send();
```

That way we can give information if the request is valid and if it could be handled correctly, without modifying the payload!

## With expected response

All these sendings where a one way ticket, but let us have a look how we can listen for a response.

```java
hagrid.wizard().topic("chat")
    .payload("How are you?")
    .sendAndWait(String.class)
    .thenAccept(stringHagridPacket -> {
        if(stringHagridPacket.getStatus().isOk()) {
            // "That is good to hear!"
        } else {
            // "I hope you get better soon .."
        }
    });
```

For that we have to specify which kind of payload we wait for. For our example it is always a `String`, but if the serdes of the topic is set to handle more abstract classes (e.g. `CloudPacket`), then we can pass on a specific implementation of that (e.g. `BroadcastMessageCloudPacket`).

If no response is being sent back, the listener will timeout. We can handle it like this:

```java
hagrid.wizard().topic("chat")
    .payload("How are you?")
    .sendAndWait(String.class)
    .thenAccept(stringHagridPacket -> {
        if(stringHagridPacket.getStatus().isTimeout()) {
            // we did not get a response
            return;
        }
        
        // do something
    });
```

# Custom service implementation

In the brief example at the beginning we showed how it would look like when using Grape. But sometimes you need to access an actual implementation of the `HagridService`. Or you want to create your own one.

Let us have a look at that.

## Preparation

To prepare using a custom implementation we definitely have to implement some custom classes first.

- **HagridConnectionHandler**: `ConnectionHandler` is an interface and this class is a small abstract default implementation of that. We use the connection handler to manage the connection to the external broker.
- **HagridSubscriber**: The subscriber is **the** instance to subscribe to topics and to listen for packets. The `DownstreamHandler` will delegate the connection to it.
- **HagridPublisher**: The publisher is **the** instance for sending packets. The `UpstreamHandler` will delegate the connection to it.

## HagridService

Now we can create a custom class and let it implement `HagridService`. You will see that there are specific methods that need to return one of the handlers.

```java
public class CustomHagridService implements HagridService {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final PropertiesConfig hagridConfig;
    
    private final CustomConnectionHandler connectionHandler;
    private final HagridUpstreamHandler upstreamHandler;
    private final HagridDownstreamHandler downstreamHandler;
    private final HagridCommunicationHandler communicationHandler;

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public PropertiesConfig getConfiguration() {
        return hagridConfig;
    }

    @Override
    public PacketWizard wizard() {
        return new HagridPacketWizard(this);
    }

    @Override
    public ConnectionHandler connection() {
        return connectionHandler;
    }

    @Override
    public UpstreamHandler upstream() {
        return upstreamHandler;
    }

    @Override
    public DownstreamHandler downstream() {
        return downstreamHandler;
    }

    @Override
    public CommunicationHandler communication() {
        return communicationHandler;
    }

}
```

But you can see that we have default implementations for the most part. But they are of course not in the `hagrid-api` Repository, but instead in the `hagrid-core`. You can import it like that:

```xml
<dependency>
    <groupId>dev.volix.rewinside.odyssey.hagrid</groupId>
    <artifactId>hagrid-core</artifactId>
    <version>1.2.0</version>
</dependency>
```

## Kafka implementation

At default we have a Kafka implementation that one can use and is recommended to do so.

For that we just have to import the `hagrid-kafka` implementation

```xml
<dependency>
    <groupId>dev.volix.rewinside.odyssey.hagrid</groupId>
    <artifactId>hagrid-kafka</artifactId>
    <version>0.7.0</version>
</dependency>
```

And create a `KafkaHagridService` instance.

```java
HagridService hagrid = KafkaHagridService.create()
    .withBrokerAddress("localhost:9092")
    .withBrokerId("my-microservice")
    .withAuth(KafkaAuth.forBasicAuth("user", "password"))
    .build();
try {
    hagrid.connect();
} catch (HagridConnectionException e) {
    // can not connect to Kafka broker
    return;
}
```

You can see that that way we also have to connect the service, as we do not have it prepared for us via Grape.
And now we can use the service just like any other implementation. Under the hood, there are more things that happen, we do not need to care.
