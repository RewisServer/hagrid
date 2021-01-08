package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridContext;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListener;
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

    private final Map<String, List<HagridListener<?>>> listenerRegistry = new HashMap<>();

    protected abstract HagridService getService();

    @Override
    public <T> void executeListeners(String topic, Direction direction, HagridContext context, HagridPacket<T> packet) {
        Class<T> payloadClass = packet.getPayload() == null ? null : (Class<T>) packet.getPayload().getClass();
        List<HagridListener<?>> listeners = new ArrayList<>(this.getListener(topic, payloadClass));

        listeners.sort(Comparator.comparingInt(HagridListener::getPriority));
        for (HagridListener<?> listener : listeners) {
            if (listener.getDirection() != null && listener.getDirection() != direction) continue;
            if (listener.getPayloadClass() == HagridPacket.class) {
                listener.execute(packet, context);
                continue;
            }
            listener.execute(packet.getPayload(), context);
        }
    }

    @Override
    public <T> boolean hasListener(String topic, Class<T> payloadClass) {
        List<HagridListener<?>> listeners = listenerRegistry.get(topic);
        if (listeners == null || listeners.isEmpty()) return false;

        return listeners.stream().anyMatch(hagridListener ->
            hagridListener.getPayloadClass() == HagridPacket.class || hagridListener.getPayloadClass().equals(payloadClass));
    }

    @Override
    public <T> void registerListener(HagridListener<T> listener) {
        List<HagridListener<?>> listeners = listenerRegistry.getOrDefault(listener.getTopic(), new ArrayList<>());
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
            if (declaredMethod.getParameterTypes()[1] != HagridContext.class) continue;
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

            this.registerListener(new HagridListener<>(topic, direction, parameter, (payload, context) -> {
                try {
                    declaredMethod.invoke(containingInstance, payload, context);

                    // if context is prepared to contain a response
                    // we automatically send it here.
                    if (context.hasResponse()) {
                        Status responseStatus = context.getResponseStatus();
                        if (responseStatus == null) responseStatus = new Status(StatusCode.OK, "");
                        Object responsePayload = context.getResponsePayload();
                        getService().wizard().respondsTo(context)
                            .status(responseStatus.getCode(), responseStatus.getMessage())
                            .payload(responsePayload)
                            .send();
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    // ignore, dont execute then ..
                }
            }, priority));
        }
    }

    @Override
    public <T> void unregisterListener(HagridListener<T> listener) {
        List<HagridListener<?>> listeners = listenerRegistry.get(listener.getTopic());
        if (listeners == null) return;
        listeners.remove(listener);
    }

    @Override
    public <T> void unregisterListener(String topic, Class<T> payloadClass) {
        List<HagridListener<?>> listeners = listenerRegistry.get(topic);
        if (listeners == null) return;
        listeners.removeIf(hagridListener -> hagridListener.getPayloadClass().equals(payloadClass));
    }

    @Override
    public void unregisterListener(String topic) {
        listenerRegistry.entrySet().removeIf(stringListEntry -> stringListEntry.getKey().equals(topic));
    }

    @Override
    public List<HagridListener<?>> getListener(String topic, Class<?> payloadClass) {
        List<HagridListener<?>> listeners = listenerRegistry.getOrDefault(topic, new ArrayList<>());
        if (listeners.isEmpty()) return new ArrayList<>();

        return listeners.stream()
            .filter(hagridListener -> hagridListener.getPayloadClass().equals(payloadClass))
            .collect(Collectors.toList());
    }

    @Override
    public List<HagridListener<?>> getListener(String topic) {
        return this.listenerRegistry.getOrDefault(topic, new ArrayList<>());
    }

}
