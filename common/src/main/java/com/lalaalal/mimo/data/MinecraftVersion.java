package com.lalaalal.mimo.data;

public record MinecraftVersion(Type type, String name) {
    public static MinecraftVersion of(int major, int minor) {
        return new MinecraftVersion(Type.STABLE, shrinkName("1.%d.%d".formatted(major, minor)));
    }

    private static String shrinkName(String name) {
        return name.replaceFirst("\\.0$", "");
    }

    public static MinecraftVersion of(String name) {
        return new MinecraftVersion(Type.byVersionName(name), shrinkName(name));
    }

    @Override
    public String toString() {
        return name;
    }

    public enum Type {
        STABLE, SNAPSHOT;

        public static Type byVersionName(String name) {
            if (name.matches("^1(\\.[0-9]+)+"))
                return STABLE;
            return SNAPSHOT;
        }
    }
}
