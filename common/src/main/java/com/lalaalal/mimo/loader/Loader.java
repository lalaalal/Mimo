package com.lalaalal.mimo.loader;

public record Loader(Type type, String version) {
    public Loader(String type, String version) {
        this(Type.valueOf(type.toUpperCase()), version);
    }

    @Override
    public String toString() {
        return type + " " + version;
    }

    public enum Type {
        FABRIC, NEOFORGE;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
