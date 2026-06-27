package com.lalaalal.mimo.registry;

import com.lalaalal.mimo.exception.MessageComponentException;

import java.util.*;

public class Registry<T> implements Iterable<RegistryItem<T>> {
    public static final Registry<Registry<?>> ROOT = new Registry<>("root");

    private final String name;
    private final Map<String, RegistryItem<T>> registry = new LinkedHashMap<>();
    private final Map<T, String> byElement = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> create(String key) {
        RegistryItem<Registry<?>> registry = ROOT.get(key);
        if (registry == null)
            registry = ROOT.registerAndGetItem(key, new Registry<>(key));
        return (Registry<T>) registry.value();
    }

    private Registry(String name) {
        this.name = name;
    }

    public boolean contains(String name) {
        return registry.containsKey(name);
    }

    public T register(String key, T value) {
        byElement.put(value, key);
        registry.put(key, new RegistryItem<>(new RegistryKey(name, key), value));
        return value;
    }

    public T register(RegistryKey key, T value) {
        byElement.put(value, key.name());
        registry.put(key.name(), new RegistryItem<>(key, value));
        return value;
    }

    public RegistryItem<T> registerAndGetItem(String key, T value) {
        byElement.put(value, key);
        RegistryItem<T> item = new RegistryItem<>(new RegistryKey(name, key), value);
        registry.put(key, item);
        return item;
    }

    public RegistryItem<T> registerAndGetItem(RegistryKey key, T value) {
        byElement.put(value, key.name());
        RegistryItem<T> item = new RegistryItem<>(key, value);
        registry.put(key.name(), item);
        return item;
    }

    public String findKey(T value) {
        return byElement.get(value);
    }

    public RegistryItem<T> get(String key) {
        return registry.get(key);
    }

    public Optional<T> getValue(String key) {
        if (contains(key))
            return Optional.of(registry.get(key).value());
        return Optional.empty();
    }

    public T getOrThrow(String key) {
        return getValue(key).orElseThrow(() -> new MessageComponentException("No such element in \"" + name + "\" : " + key));
    }

    public RegistryItem<T> get(RegistryKey key) {
        return registry.get(key.name());
    }

    public Optional<T> getValue(RegistryKey key) {
        if (contains(key.name()))
            return Optional.of(registry.get(key.name()).value());
        return Optional.empty();
    }

    public T getOrThrow(RegistryKey key) {
        return getValue(key.name()).orElseThrow(() -> new MessageComponentException("No such element : " + name));
    }

    public Set<String> keySet() {
        return registry.keySet();
    }

    @Override
    public Iterator<RegistryItem<T>> iterator() {
        return registry.values().iterator();
    }
}
