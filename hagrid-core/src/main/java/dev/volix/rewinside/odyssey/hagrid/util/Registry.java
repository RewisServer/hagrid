package dev.volix.rewinside.odyssey.hagrid.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Tobias Büser
 */
public class Registry<K, V> {

    private final Map<K, V> registry = new ConcurrentHashMap<>();
    private final Map<K, List<Consumer<V>>> callbacks = new ConcurrentHashMap<>();

    public Map<K, V> getBackingMap() {
        return new HashMap<>(this.registry);
    }

    public boolean has(final K key) {
        return this.registry.containsKey(key);
    }

    public boolean has(final Predicate<V> filter) {
        return this.find(filter).isPresent();
    }

    public void register(final K key, final V value) {
        if (this.has(key)) {
            throw new IllegalStateException("There is already a value registered with key " + key);
        }
        this.registry.put(key, value);

        final List<Consumer<V>> callbacks = this.callbacks.remove(key);
        if (callbacks != null) {
            callbacks.forEach(c -> c.accept(value));
        }
    }

    public void unregister(final K key) {
        this.registry.remove(key);
    }

    public void unregister(final Predicate<V> filter) {
        this.registry.entrySet().removeIf(kvEntry -> filter.test(kvEntry.getValue()));
    }

    protected Optional<V> find(final Predicate<V> filter, final K key) {
        if (key != null && this.has(key)) {
            return Optional.of(this.registry.get(key));
        }
        if (filter == null) {
            return Optional.ofNullable(this.registry.get(key));
        }
        return this.registry.values().stream().filter(filter).findFirst();
    }

    public Optional<V> find(final Predicate<V> filter) {
        return this.find(filter, null);
    }

    public Optional<V> find(final K key) {
        return this.find(null, key);
    }

    protected V getOrDefault(final Predicate<V> filter, final K key, final V defaultValue) {
        return this.find(filter, key).orElse(defaultValue);
    }

    public V getOrDefault(final K key, final V defaultValue) {
        return this.getOrDefault(null, key, defaultValue);
    }

    public V getOrDefault(final Predicate<V> filter, final V defaultValue) {
        return this.getOrDefault(filter, null, defaultValue);
    }

    protected V getOrNull(final Predicate<V> filter, final K key) {
        return this.getOrDefault(filter, key, (V) null);
    }

    public V getOrNull(final K key) {
        return this.getOrNull(null, key);
    }

    public V getOrNull(final Predicate<V> filter) {
        return this.getOrNull(filter, null);
    }

    protected void get(final Predicate<V> filter, final K key, final Consumer<V> callback) {
        final Optional<V> provider = this.find(key);

        if (provider.isPresent()) {
            callback.accept(provider.get());
            return;
        }

        final List<Consumer<V>> callbacks = this.callbacks.getOrDefault(key, new ArrayList<>());
        callbacks.add(new PredicatedConsumer<>(filter, callback));
        this.callbacks.put(key, callbacks);
    }

    public void get(final K key, final Consumer<V> callback) {
        this.get(null, key, callback);
    }

    public void get(final Predicate<V> filter, final Consumer<V> callback) {
        this.get(filter, null, callback);
    }

    protected CompletableFuture<V> get(final Predicate<V> filter, final K key) {
        final CompletableFuture<V> future = new CompletableFuture<>();
        this.get(key, future::complete);
        return future;
    }

    public CompletableFuture<V> get(final K key) {
        return this.get(null, key);
    }

    public CompletableFuture<V> get(final Predicate<V> filter) {
        return this.get(filter, (K) null);
    }

    protected static class PredicatedConsumer<V> implements Consumer<V> {

        private final Predicate<V> predicate;
        private final Consumer<V> consumer;

        public PredicatedConsumer(final Predicate<V> predicate, final Consumer<V> consumer) {
            this.predicate = predicate;
            this.consumer = consumer;
        }

        @Override
        public void accept(final V v) {
            if (this.predicate == null || this.predicate.test(v)) {
                this.consumer.accept(v);
            }
        }

    }

}
