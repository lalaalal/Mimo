package com.lalaalal.mimo.logging;

public enum Level {
    ERROR(ConsoleColor.RED.foreground()),
    WARNING(ConsoleColor.YELLOW.foreground()),
    INFO(ConsoleColor.GREEN.foreground()),
    DEBUG(ConsoleColor.WHITE.foreground()),
    VERBOSE(ConsoleColor.WHITE.foreground());

    private final int priority;
    public final Style style;

    public static Level get(String name) {
        return valueOf(name.toUpperCase());
    }

    Level(Style style) {
        this.priority = ordinal();
        this.style = style;
    }

    public boolean shouldLog(Level level) {
        return priority >= level.priority;
    }
}
