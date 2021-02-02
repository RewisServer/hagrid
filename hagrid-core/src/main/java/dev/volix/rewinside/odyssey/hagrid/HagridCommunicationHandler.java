package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.exception.HagridListenerExecutionException;
import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListener;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerMethod;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListens;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridResponds;
import dev.volix.rewinside.odyssey.hagrid.listener.Priority;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import dev.volix.rewinside.odyssey.hagrid.serdes.HagridSerdes;
import dev.volix.rewinside.odyssey.hagrid.topic.HagridTopic;
import dev.volix.rewinside.odyssey.hagrid.topic.HagridTopicGroup;
import dev.volix.rewinside.odyssey.hagrid.topic.TopicProperties;
import dev.volix.rewinside.odyssey.hagrid.util.DaemonThreadFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Tobias Büser
 */
public class HagridCommunicationHandler implements CommunicationHandler {

    private final HagridService service;

    private static final Pattern TOPIC_REGEX = Pattern.compile("^(?:\\w+)(?:-(?:\\w+|\\*))*$");

    private final Map<String, HagridTopicGroup> topicGroupRegistry = new HashMap<>();
    private final Map<Pattern, List<HagridListener>> listenerRegistry = new ConcurrentHashMap<>();

    public HagridCommunicationHandler(final HagridService service) {
        this.service = service;

        final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1, new DaemonThreadFactory());
        threadPool.scheduleAtFixedRate(new CleanupTask(this.listenerRegistry), 2, 2, TimeUnit.SECONDS);
    }

    private String getTopicPrefix(final String pattern) {
        final String[] parts = pattern.split("-");
        return parts[0];
    }

    @Override
    public HagridTopicGroup getTopicGroup(final String prefix) {
        return this.topicGroupRegistry.get(prefix);
    }

    @Override
    public <T> HagridTopic<T> getTopic(final String pattern) {
        if (!TOPIC_REGEX.matcher(pattern).matches()) {
            throw new IllegalArgumentException("pattern must be in kebab-case");
        }
        final String prefix = this.getTopicPrefix(pattern);

        final HagridTopicGroup topicGroup = this.getTopicGroup(prefix);
        if (topicGroup == null) throw new IllegalStateException("no topic group found for given prefix");

        final HagridTopic<?> topic = topicGroup.getTopicExactly(pattern);
        if (topic != null) return (HagridTopic<T>) topic;

        return (HagridTopic<T>) topicGroup.getMostFitting(pattern);
    }

    @Override
    public void registerTopic(final String pattern, final HagridSerdes<?> serdes, final TopicProperties properties) {
        if (!TOPIC_REGEX.matcher(pattern).matches()) {
            throw new IllegalArgumentException("pattern must be in kebab-case");
        }
        final HagridTopic<?> topic = new HagridTopic<>(pattern, serdes, properties);
        final String prefix = this.getTopicPrefix(pattern);

        HagridTopicGroup topicGroup = this.getTopicGroup(prefix);

        if (topicGroup == null) {
            topicGroup = new HagridTopicGroup(topic);
            this.topicGroupRegistry.put(prefix, topicGroup);
        } else {
            topicGroup.add(topic);
        }

        this.service.downstream().addToSubscriber(topic);
    }

    @Override
    public void unregisterTopic(final String pattern) {
        if (!TOPIC_REGEX.matcher(pattern).matches()) return;
        final String prefix = this.getTopicPrefix(pattern);

        final HagridTopicGroup topicGroup = this.getTopicGroup(prefix);
        if (topicGroup == null) return;

        final HagridTopic<?> topic = topicGroup.getTopicExactly(pattern);
        if (topic == null) return;
        topicGroup.remove(pattern);

        this.service.downstream().removeFromSubscriber(topic);
    }

    @Override
    public <T> void executeListeners(final String topic, final Direction direction, final HagridPacket<T> packet) {
        final Optional<T> payloadOptional = packet.getPayload();

        final Class<?> payloadClass = payloadOptional.<Class<?>>map(T::getClass).orElse(null);
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
            final T payload = listener.getPayloadClass() == null
                || listener.getPayloadClass() == Void.class ? null : payloadOptional.orElse(null);

            Throwable executionError = null;

            try {
                listener.execute(payload, packet, response);
            } catch (final HagridListenerExecutionException ex) {
                executionError = ex;
            }

            if (executionError != null && listener.isResponsive()) {
                // some error during execution, but the listener
                // asks for a response nonetheless
                final Throwable cause = executionError.getCause();

                this.service.wizard().respondsTo(packet)
                    .status(StatusCode.INTERNAL, cause == null ? executionError.getMessage() : cause.getMessage())
                    .send();
            } else if (response.getPayload() != null || response.getStatus() != null) {
                // everything worked fine and he filled the response object
                Status responseStatus = response.getStatus();
                if (responseStatus == null) responseStatus = new Status(StatusCode.OK, "");
                final Object responsePayload = response.getPayload();

                this.service.wizard().respondsTo(packet)
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
        final Pattern pattern = HagridTopic.getTopicAsRegex(topic);
        final List<HagridListener> listeners = this.listenerRegistry.get(pattern);
        if (listeners == null || listeners.isEmpty()) return false;

        return listeners.stream().anyMatch(hagridListener ->
            hagridListener.getPayloadClass() == HagridPacket.class || hagridListener.getPayloadClass().equals(payloadClass));
    }

    @Override
    public void registerListener(final HagridListener listener) {
        if (!TOPIC_REGEX.matcher(listener.getTopic()).matches()) {
            throw new IllegalArgumentException("listener topic needs to be in kebab-case");
        }
        final Pattern pattern = HagridTopic.getTopicAsRegex(listener.getTopic());

        final List<HagridListener> listeners = this.listenerRegistry.getOrDefault(pattern, new ArrayList<>());
        listeners.add(listener);

        this.listenerRegistry.put(pattern, listeners);

        listener.setRegisteredAt(System.currentTimeMillis());
    }

    @Override
    public void registerListeners(final Object containingInstance) {
        final Class<?> clazz = containingInstance.getClass();
        final HagridListens listensClassAnnotation = clazz.getAnnotation(HagridListens.class);
        final HagridResponds respondsClassAnnotation = clazz.getAnnotation(HagridResponds.class);

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
            final String topic = listensClassAnnotation != null ?
                listensClassAnnotation.topic().isEmpty() ? annotation.topic() : listensClassAnnotation.topic()
                : annotation.topic();
            final Direction direction = listensClassAnnotation != null ?
                listensClassAnnotation.direction() == Direction.DOWNSTREAM ? annotation.direction() : listensClassAnnotation.direction()
                : annotation.direction();
            final int priority = listensClassAnnotation != null ?
                listensClassAnnotation.priority() == Priority.MEDIUM ? annotation.priority() : listensClassAnnotation.priority()
                : annotation.priority();

            final boolean isResponsive = respondsClassAnnotation != null
                || declaredMethod.getAnnotation(HagridResponds.class) != null;

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
                    } catch (final InvocationTargetException e) {
                        throw new HagridListenerExecutionException(topic, parameter, e.getCause());
                    } catch (final Exception e) {
                        throw new HagridListenerExecutionException(topic, parameter, e);
                    }
                }
            }).topic(topic).direction(direction).payloadClass(parameter)
                .priority(priority).responsive(isResponsive).build());
        }
    }

    @Override
    public void unregisterListener(final HagridListener listener) {
        final Pattern pattern = HagridTopic.getTopicAsRegex(listener.getTopic());
        final List<HagridListener> listeners = this.listenerRegistry.get(pattern);
        if (listeners == null) return;
        listeners.remove(listener);
    }

    @Override
    public void unregisterListener(final String topic, final Class<?> payloadClass) {
        final Pattern pattern = HagridTopic.getTopicAsRegex(topic);
        final List<HagridListener> listeners = this.listenerRegistry.get(pattern);
        if (listeners == null) return;
        listeners.removeIf(hagridListener -> hagridListener.getPayloadClass().equals(payloadClass));
    }

    @Override
    public void unregisterListener(final String topic) {
        final Pattern pattern = HagridTopic.getTopicAsRegex(topic);
        this.listenerRegistry.entrySet().removeIf(stringListEntry -> stringListEntry.getKey().equals(pattern));
    }

    @Override
    public List<HagridListener> getListener(final String topic, final Class<?> payloadClass) {
        final List<HagridListener> listeners = new ArrayList<>();
        for (final Pattern pattern : this.listenerRegistry.keySet()) {
            if (pattern.matcher(topic).matches()) {
                listeners.addAll(this.listenerRegistry.get(pattern));
            }
        }
        if (listeners.isEmpty()) return new ArrayList<>();

        return listeners.stream()
            .filter(hagridListener -> {
                if (payloadClass == null) {
                    return hagridListener.getPayloadClass() == null || hagridListener.getPayloadClass() == Void.class
                        || hagridListener.getListenId() != null;
                }
                return hagridListener.getPayloadClass() == null
                    || hagridListener.getPayloadClass() == Void.class
                    || hagridListener.getPayloadClass().isAssignableFrom(payloadClass);
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<HagridListener> getListener(final String topic) {
        final Pattern pattern = HagridTopic.getTopicAsRegex(topic);
        return this.listenerRegistry.getOrDefault(pattern, new ArrayList<>());
    }

    private class CleanupTask implements Runnable {

        private final Map<Pattern, List<HagridListener>> listenerRegistry;

        public CleanupTask(final Map<Pattern, List<HagridListener>> listenerRegistry) {
            this.listenerRegistry = listenerRegistry;
        }

        @Override
        public void run() {
            final long current = System.currentTimeMillis();
            for (final Pattern pattern : this.listenerRegistry.keySet()) {
                final List<HagridListener> listenersCopy = new ArrayList<>(this.listenerRegistry.get(pattern));

                for (final HagridListener listener : listenersCopy) {
                    if (listener.getTimeoutInSeconds() <= 0) continue;
                    final long timeoutAt = listener.getRegisteredAt() + (listener.getTimeoutInSeconds() * 1000);

                    if (current >= timeoutAt) {
                        HagridCommunicationHandler.this.unregisterListener(listener);
                        listener.executeTimeout();
                    }
                }
            }
        }

    }

}
