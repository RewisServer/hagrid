package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListener;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerMethod;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerRegistry;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListens;
import dev.volix.rewinside.odyssey.hagrid.listener.Priority;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Tobias Büser
 */
abstract class UglyHagridListenerRegistry implements HagridListenerRegistry {

    private final Map<String, List<HagridListener>> listenerRegistry = new HashMap<>();

    protected abstract HagridService getService();

    @Override
    public <T> void executeListeners(String topic, Direction direction, HagridPacket<T> packet) {
        Class<?> payloadClass = packet.getPayload() == null ? null : packet.getPayload().getClass();
        List<HagridListener> listeners = new ArrayList<>(this.getListener(topic, payloadClass));

        listeners.sort(Comparator.comparingInt(HagridListener::getPriority));
        for (HagridListener listener : listeners) {
            if (listener.getDirection() != null && listener.getDirection() != direction) {
                continue;
            }
            if (listener.getRequestId() != null && !packet.getId().equals(listener.getRequestId())) {
                continue;
            }

            HagridResponse response = new HagridResponse();
            if (listener.getPayloadClass() == null) {
                listener.execute(null, packet, response);
                continue;
            }
            listener.execute(packet.getPayload(), packet, response);

            if (response.getPayload() != null || response.getStatus() != null) {
                Status responseStatus = response.getStatus();
                if (responseStatus == null) responseStatus = new Status(StatusCode.OK, "");
                Object responsePayload = response.getPayload();

                this.getService().wizard().respondsTo(packet)
                    .status(responseStatus.getCode(), responseStatus.getMessage())
                    .payload(responsePayload)
                    .send();
            }

            if (listener.getRequestId() != null) {
                // a requestid is unique, this listener needs to be unregistered
                this.unregisterListener(listener);
            }
        }
    }

    @Override
    public boolean hasListener(String topic, Class<?> payloadClass) {
        List<HagridListener> listeners = listenerRegistry.get(topic);
        if (listeners == null || listeners.isEmpty()) return false;

        return listeners.stream().anyMatch(hagridListener ->
            hagridListener.getPayloadClass() == HagridPacket.class || hagridListener.getPayloadClass().equals(payloadClass));
    }

    @Override
    public void registerListener(HagridListener listener) {
        List<HagridListener> listeners = listenerRegistry.getOrDefault(listener.getTopic(), new ArrayList<>());
        listeners.add(listener);
        listenerRegistry.put(listener.getTopic(), listeners);
    }

    @Override
    public void registerListeners(Object containingInstance) {
        Class<?> clazz = containingInstance.getClass();
        HagridListens clazzAnnotation = clazz.getAnnotation(HagridListens.class);

        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            HagridListens annotation = declaredMethod.getAnnotation(HagridListens.class);
            if (annotation == null) continue;

            if (!declaredMethod.getReturnType().equals(void.class)) continue;
            if (declaredMethod.getParameterCount() != 2) continue;
            if (declaredMethod.getParameterTypes()[1] != HagridPacket.class) continue;
            Class<?> parameter = declaredMethod.getParameterTypes()[0];

            // if the enclosing class already contains such an annotation
            // we can override specific values if necessary
            String topic = clazzAnnotation != null ?
                clazzAnnotation.topic().isEmpty() ? annotation.topic() : clazzAnnotation.topic()
                : annotation.topic();
            Direction direction = clazzAnnotation != null ?
                clazzAnnotation.direction() == Direction.DOWNSTREAM ? annotation.direction() : clazzAnnotation.direction()
                : annotation.direction();
            int priority = clazzAnnotation != null ?
                clazzAnnotation.priority() == Priority.MEDIUM ? annotation.priority() : clazzAnnotation.priority()
                : annotation.priority();

            this.registerListener(HagridListener.builder(new HagridListenerMethod() {
                @Override
                public <T> void listen(T payload, HagridPacket<T> req, HagridResponse response) {
                    try {
                        declaredMethod.invoke(containingInstance, payload, response);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        // ignore, dont execute then ..
                    }
                }
            }).topic(topic).direction(direction).payloadClass(parameter).priority(priority).build());
        }
    }

    @Override
    public void unregisterListener(HagridListener listener) {
        List<HagridListener> listeners = listenerRegistry.get(listener.getTopic());
        if (listeners == null) return;
        listeners.remove(listener);
    }

    @Override
    public void unregisterListener(String topic, Class<?> payloadClass) {
        List<HagridListener> listeners = listenerRegistry.get(topic);
        if (listeners == null) return;
        listeners.removeIf(hagridListener -> hagridListener.getPayloadClass().equals(payloadClass));
    }

    @Override
    public void unregisterListener(String topic) {
        listenerRegistry.entrySet().removeIf(stringListEntry -> stringListEntry.getKey().equals(topic));
    }

    @Override
    public List<HagridListener> getListener(String topic, Class<?> payloadClass) {
        List<HagridListener> listeners = listenerRegistry.getOrDefault(topic, new ArrayList<>());
        if (listeners.isEmpty()) return new ArrayList<>();

        return listeners.stream()
            .filter(hagridListener -> hagridListener.getPayloadClass() == null || payloadClass.equals(hagridListener.getPayloadClass()))
            .collect(Collectors.toList());
    }

    @Override
    public List<HagridListener> getListener(String topic) {
        return this.listenerRegistry.getOrDefault(topic, new ArrayList<>());
    }

}
