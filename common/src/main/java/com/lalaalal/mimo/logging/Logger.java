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
        if (elements.length < 5)
            return currentThread.getName();
        StackTraceElement element = elements[4];
        return currentThread.getName() + "/" + element.getClassName().substring(element.getClassName().lastIndexOf('.') + 1);
    }

    public void log(Level level, String message) {
        if (this.level.shouldLog(level)) {
            Component line = Component.complex(
                    getTimestamp(), Component.SPACE,
                    Component.of("[" + getCaller() + "]", useStyle)
                            .with(ConsoleColor.CYAN.foreground()), Component.SPACE,
                    Component.of("[" + level.name() + "]", useStyle)
                            .with(level.style),
                    Component.of(": " + message, useStyle)
                            .with(Style.DEFAULT),
                    Component.NEW_LINE
            );
            line.print(printStream);
        }
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
}
