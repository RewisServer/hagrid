package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListener;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerMethod;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerRegistry;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListens;
import dev.volix.rewinside.odyssey.hagrid.listener.Priority;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import dev.volix.rewinside.odyssey.hagrid.util.DaemonThreadFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Tobias Büser
 */
public abstract class StandardHagridListenerRegistry implements HagridListenerRegistry {

    private final Map<String, List<HagridListener>> listenerRegistry = new ConcurrentHashMap<>();

    protected abstract HagridService getService();

    public StandardHagridListenerRegistry() {
        final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1, new DaemonThreadFactory());
        threadPool.scheduleAtFixedRate(new CleanupTask(this.listenerRegistry), 2, 2, TimeUnit.SECONDS);
    }

    @Override
    public <T> void executeListeners(final String topic, final Direction direction, final HagridPacket<T> packet) {
        final Class<?> payloadClass = packet.getPayload() == null ? null : packet.getPayload().getClass();
        final List<HagridListener> listeners = new ArrayList<>(this.getListener(topic, payloadClass));

        listeners.sort(Comparator.comparingInt(HagridListener::getPriority));
        for (final HagridListener listener : listeners) {
            if (listener.getDirection() != null && listener.getDirection() != direction) {
                continue;
            }
            if (listener.getListenId() != null && !listener.getListenId().equals(packet.getRequestId())) {
                continue;
            }

            final HagridResponse response = new HagridResponse();
            if (listener.getPayloadClass() == null) {
                listener.execute(null, packet, response);
                continue;
            }
            listener.execute(packet.getPayload(), packet, response);

            if (response.getPayload() != null || response.getStatus() != null) {
                Status responseStatus = response.getStatus();
                if (responseStatus == null) responseStatus = new Status(StatusCode.OK, "");
                final Object responsePayload = response.getPayload();

                this.getService().wizard().respondsTo(packet)
                    .status(responseStatus.getCode(), responseStatus.getMessage())
                    .payload(responsePayload)
                    .send();
            }

            if (listener.getListenId() != null) {
                // a requestid is unique, this listener needs to be unregistered
                this.unregisterListener(listener);
            }
        }
    }

    @Override
    public boolean hasListener(final String topic, final Class<?> payloadClass) {
        final List<HagridListener> listeners = this.listenerRegistry.get(topic);
        if (listeners == null || listeners.isEmpty()) return false;

        return listeners.stream().anyMatch(hagridListener ->
            hagridListener.getPayloadClass() == HagridPacket.class || hagridListener.getPayloadClass().equals(payloadClass));
    }

    @Override
    public void registerListener(final HagridListener listener) {
        final List<HagridListener> listeners = this.listenerRegistry.getOrDefault(listener.getTopic(), new ArrayList<>());
        listeners.add(listener);
        this.listenerRegistry.put(listener.getTopic(), listeners);

        listener.setRegisteredAt(System.currentTimeMillis());
    }

    @Override
    public void registerListeners(final Object containingInstance) {
        final Class<?> clazz = containingInstance.getClass();
        final HagridListens clazzAnnotation = clazz.getAnnotation(HagridListens.class);

        for (final Method declaredMethod : clazz.getDeclaredMethods()) {
            final HagridListens annotation = declaredMethod.getAnnotation(HagridListens.class);
            if (annotation == null) continue;

            if (!declaredMethod.getReturnType().equals(void.class)) continue;
            final int parameterCount = declaredMethod.getParameterCount();

            if (parameterCount < 1 || parameterCount > 3) continue;
            if (parameterCount >= 2 && declaredMethod.getParameterTypes()[1] != HagridPacket.class) continue;
            if (parameterCount >= 3 && declaredMethod.getParameterTypes()[2] != HagridResponse.class) continue;
            final Class<?> parameter = declaredMethod.getParameterTypes()[0];

            // if the enclosing class already contains such an annotation
            // we can override specific values if necessary
            final String topic = clazzAnnotation != null ?
                clazzAnnotation.topic().isEmpty() ? annotation.topic() : clazzAnnotation.topic()
                : annotation.topic();
            final Direction direction = clazzAnnotation != null ?
                clazzAnnotation.direction() == Direction.DOWNSTREAM ? annotation.direction() : clazzAnnotation.direction()
                : annotation.direction();
            final int priority = clazzAnnotation != null ?
                clazzAnnotation.priority() == Priority.MEDIUM ? annotation.priority() : clazzAnnotation.priority()
                : annotation.priority();

            this.registerListener(HagridListener.builder(new HagridListenerMethod() {
                @Override
                public <T> void listen(final T payload, final HagridPacket<T> req, final HagridResponse response) {
                    try {
                        if (parameterCount == 1) {
                            declaredMethod.invoke(containingInstance, payload);
                        } else if (parameterCount == 2) {
                            declaredMethod.invoke(containingInstance, payload, req);
                        } else {
                            declaredMethod.invoke(containingInstance, payload, req, response);
                        }
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        // ignore, dont execute then ..
                    }
                }
            }).topic(topic).direction(direction).payloadClass(parameter).priority(priority).build());
        }
    }

    @Override
    public void unregisterListener(final HagridListener listener) {
        final List<HagridListener> listeners = this.listenerRegistry.get(listener.getTopic());
        if (listeners == null) return;
        listeners.remove(listener);
    }

    @Override
    public void unregisterListener(final String topic, final Class<?> payloadClass) {
        final List<HagridListener> listeners = this.listenerRegistry.get(topic);
        if (listeners == null) return;
        listeners.removeIf(hagridListener -> hagridListener.getPayloadClass().equals(payloadClass));
    }

    @Override
    public void unregisterListener(final String topic) {
        this.listenerRegistry.entrySet().removeIf(stringListEntry -> stringListEntry.getKey().equals(topic));
    }

    @Override
    public List<HagridListener> getListener(final String topic, final Class<?> payloadClass) {
        final List<HagridListener> listeners = this.listenerRegistry.getOrDefault(topic, new ArrayList<>());
        if (listeners.isEmpty()) return new ArrayList<>();

        return listeners.stream()
            .filter(hagridListener -> {
                if (payloadClass == null) {
                    return hagridListener.getPayloadClass() == null
                        || hagridListener.getListenId() != null;
                }
                return hagridListener.getPayloadClass() == null
                    || hagridListener.getPayloadClass().equals(payloadClass);
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<HagridListener> getListener(final String topic) {
        return this.listenerRegistry.getOrDefault(topic, new ArrayList<>());
    }

    private class CleanupTask implements Runnable {

        private final Map<String, List<HagridListener>> listenerRegistry;

        public CleanupTask(final Map<String, List<HagridListener>> listenerRegistry) {
            this.listenerRegistry = listenerRegistry;
        }

        @Override
        public void run() {
            final long current = System.currentTimeMillis();
            for (final String topic : this.listenerRegistry.keySet()) {
                final List<HagridListener> listenersCopy = new ArrayList<>(this.listenerRegistry.get(topic));

                for (final HagridListener listener : listenersCopy) {
                    if (listener.getTimeoutInSeconds() <= 0) continue;
                    final long timeoutAt = listener.getRegisteredAt() + (listener.getTimeoutInSeconds() * 1000);

                    if (current >= timeoutAt) {
                        StandardHagridListenerRegistry.this.unregisterListener(listener);
                        listener.executeTimeout();
                    }
                }
            }
        }

    }

}
