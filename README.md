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
HagridService service = future.get();
```

After that we can now start sending packets.

```java
service.communication().registerTopic("chat", new StringHagridSerdes());

service.wizard().topic("chat")
    .payload("Hello there!")
    .send();
```

# Topics

