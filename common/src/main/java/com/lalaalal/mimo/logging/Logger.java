package com.lalaalal.mimo.logging;

import java.io.PrintStream;
import java.time.LocalDateTime;

public class Logger {
    private final PrintStream printStream;
    private final boolean useStyle;
    private Level level = Level.INFO;

    public static Logger stdout() {
        return new Logger(System.out, true);
    }

    public Logger(PrintStream printStream, boolean useStyle) {
        this.printStream = printStream;
        this.useStyle = useStyle;
    }

    private Component getTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        return Component.of(String.format("[%02d:%02d:%02d]", now.getHour(), now.getMinute(), now.getSecond()), useStyle)
                .with(ConsoleColor.BLUE.foreground());
    }

    private String getCaller() {
        Thread currentThread = Thread.currentThread();
        StackTraceElement[] elements = currentThread.getStackTrace();
        boolean firstMatch = true;
        for (StackTraceElement element : elements) {
            if (!element.getClassName().equals(Logger.class.getName())) {
                if (!firstMatch)
                    return currentThread.getName() + "/" + element.getClassName().substring(element.getClassName().lastIndexOf('.') + 1);
            } else {
                firstMatch = false;
            }
        }
        return currentThread.getName();
    }

    public void log(Level level, Component message) {
        if (this.level.shouldLog(level)) {
            Component line = Component.complex(
                    getTimestamp(), Component.SPACE,
                    Component.of("[" + getCaller() + "]", useStyle)
                            .with(ConsoleColor.CYAN.foreground()), Component.SPACE,
                    Component.of("[" + level.name() + "]", useStyle)
                            .with(level.style),
                    Component.of(": ", useStyle)
                            .with(Style.DEFAULT),
                    message,
                    Component.NEW_LINE
            );
            line.print(printStream);
        }
    }

    public void log(Level level, String message) {
        log(level, Component.withDefault(message));
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void verbose(String message) {
        log(Level.VERBOSE, message);
    }

    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void warning(String message) {
        log(Level.WARNING, message);
    }

    public void error(String message) {
        log(Level.ERROR, message);
    }

    public void verbose(Component message) {
        log(Level.VERBOSE, message);
    }

    public void debug(Component message) {
        log(Level.DEBUG, message);
    }

    public void info(Component message) {
        log(Level.INFO, message);
    }

    public void warning(Component message) {
        log(Level.WARNING, message);
    }

    public void error(Component message) {
        log(Level.ERROR, message);
    }
}
