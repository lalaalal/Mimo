package com.lalaalal.mimo.registry;

public record RegistryKey(String registry, String name) {
    public static RegistryKey of(String key) {
        String[] result = key.split(":");
        if (result.length != 2)
            throw new IllegalArgumentException("Invalid key: " + key);
        return new RegistryKey(result[0], result[1]);
    }

    @Override
    public String toString() {
        return registry + ":" + name;
    }
}
