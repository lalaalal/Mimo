package com.lalaalal.mimo.loader;

public record Loader(Type type, String version) {
    public Loader(String type, String version) {
        this(Type.get(type), version);
    }

    @Override
    public String toString() {
        return type + " " + version;
    }

    public enum Type {
        DATAPACK, FABRIC, NEOFORGE, FORGE;

        public static Type get(String name) {
            for (Type value : values()) {
                if (value.name().equalsIgnoreCase(name))
                    return value;
            }
            return null;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
