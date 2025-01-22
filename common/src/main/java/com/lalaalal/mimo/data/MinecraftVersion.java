package com.lalaalal.mimo.data;

public record MinecraftVersion(Type type, String name) {
    public static MinecraftVersion of(int major, int minor) {
        return new MinecraftVersion(Type.STABLE, "1.%d.%d".formatted(major, minor));
    }

    public static MinecraftVersion of(String name) {
        return new MinecraftVersion(Type.byVersionName(name), name);
    }

    @Override
    public String toString() {
        return name;
    }

    public enum Type {
        STABLE, SNAPSHOT;

        public static Type byVersionName(String name) {
            if (name.matches("^1\\.[0-9]+\\.[0-9]+"))
                return STABLE;
            return SNAPSHOT;
        }
    }
}
