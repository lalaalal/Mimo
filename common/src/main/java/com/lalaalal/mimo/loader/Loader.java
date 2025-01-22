package com.lalaalal.mimo.loader;

public record Loader(Type type, String version) {
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
