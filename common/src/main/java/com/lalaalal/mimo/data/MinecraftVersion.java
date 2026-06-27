package com.lalaalal.mimo.data;

import java.util.StringJoiner;

public record MinecraftVersion(Type type, String name) {
    public static MinecraftVersion legacy(int major, int minor) {
        return new MinecraftVersion(Type.STABLE, shrinkName("1.%d.%d".formatted(major, minor)));
    }

    private static String shrinkName(String name) {
        return name.replaceFirst("\\.0$", "");
    }

    public static MinecraftVersion of(String name) {
        return new MinecraftVersion(Type.byVersionName(name), shrinkName(name));
    }

    public static MinecraftVersion stable(String... numbers) {
        StringJoiner joiner = new StringJoiner(".");
        for (String number : numbers) {
            joiner.add(number);
        }
        return new MinecraftVersion(Type.STABLE, shrinkName(joiner.toString()));
    }

    @Override
    public String toString() {
        return name;
    }

    public enum Type {
        STABLE, SNAPSHOT;

        public static Type byVersionName(String name) {
            if (name.matches("^[0-9]+(\\.[0-9]+)+$"))
                return STABLE;
            return SNAPSHOT;
        }
    }
}
