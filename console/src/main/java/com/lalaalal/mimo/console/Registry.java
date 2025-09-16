package com.lalaalal.mimo.console;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Registry<T> implements Iterable<T> {
    private static final Registry<Registry<?>> ROOT = new Registry<>();

    private final Map<String, T> registry = new HashMap<>();
    private final Map<T, String> byElement = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> create(String key) {
        Registry<?> registry = ROOT.get(key);
        if (registry == null)
            registry = new Registry<>();
        return (Registry<T>) registry;
    }

    private Registry() {
    }

    public boolean contains(String name) {
        return registry.containsKey(name);
    }

    public T register(String key, T value) {
        byElement.put(value, key);
        return registry.put(key, value);
    }

    public String findKey(T value) {
        return byElement.get(value);
    }

    public T get(String name) {
        return registry.get(name);
    }

    public Set<String> keySet() {
        return registry.keySet();
    }

    @Override
    public Iterator<T> iterator() {
        return registry.values().iterator();
    }
}
