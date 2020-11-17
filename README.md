# hagrid-java

Java implementation for Hagrid. Only for him <3

# Benutzung

Zuerst musst du folgende Dependency in deine `pom.xml` eintragen:

```xml
<!-- Eigentliche Hagrid Dependency -->
<dependency>
    <groupId>dev.volix.rewinside.odyssey.hagrid</groupId>
    <artifactId>hagrid-api</artifactId>
    <version>0.1.0</version>
</dependency>

<!-- Grape stellt den HagridService bereit -->
<dependency>
    <groupId>dev.volix.lib</groupId>
    <artifactId>grape-api</artifactId>
    <version>[0.0,)</version>
    <scope>provided</scope>
</dependency>
```

Und dann in der `plugin.yml`:

```yml
...
depend: [HagridSpigot]
```

## Den Service holen

Nun kann Grape genutzt werden, um sich den *HagridService* zu besorgen, um dann damit etwaige Dinge anzustellen.

```java
Future<HagridService> future = Grape.getInstance().get(HagridService.class);
HagridService service = future.get();
```

## Ein Paket senden

Wichtig: Hagrid arbeitet mit `topics`, die sozusagen einen Down- und Upstream darstellen. Das heißt wir müssen sicherstellen, dass auch wirklich das gewollte Topic registriert ist.

```java
service.registerTopic("orchestration", new MessageHagridSerdes());

hagrid.upstream().builder()
    .topic("orchestration")
    .payload(ServerAddPacket.newBuilder()
        .setType(ServerType.GAMESERVER)
        .setName("BedWars4x2-1")
        .setIpAddress("127.0.0.1:1337")
        .build()
    )
    .send();
```

## Pakete empfangen

Hierfür können wir das Listener-System benutzen.

```java
public class ListenerClass {

    @HagridListens(topic = "orchestration")
    public void onServerAdd(ServerAddPacket packet) {
        // do something
    }

}
```

Und dann diesen Listener registrieren mit:

```java
service.registerTopic("orchestration", new MessageHagridSerdes());

service.downstream().registerListener(new ListenerClass());
```