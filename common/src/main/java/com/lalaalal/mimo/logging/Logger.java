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
        return Component.text(String.format("[%02d:%02d:%02d]", now.getHour(), now.getMinute(), now.getSecond()), useStyle)
                .with(ConsoleColor.BLUE.foreground());
    }

    private String getCaller() {
        Thread currentThread = Thread.currentThread();
        StackTraceElement[] elements = currentThread.getStackTrace();
        for (int index = elements.length - 2; index >= 0; index--) {
            StackTraceElement current = elements[index];
            StackTraceElement next = elements[index + 1];

            if (current.getClassName().equals(Logger.class.getName()))
                return currentThread.getName() + "/" + next.getClassName().substring(next.getClassName().lastIndexOf('.') + 1);
        }
        return currentThread.getName();
    }

    public void log(Level level, Component message) {
        message.lines().forEach(line -> internalLog(level, line));
    }

    private void internalLog(Level level, Component line) {
        if (this.level.shouldLog(level)) {
            Component complex = Component.complex(
                    getTimestamp(), Component.SPACE,
                    Component.text("[" + getCaller() + "]", useStyle)
                            .with(ConsoleColor.CYAN.foreground()), Component.SPACE,
                    Component.text("[" + level.name() + "]", useStyle)
                            .with(level.style),
                    Component.text(": ", useStyle)
                            .with(Style.DEFAULT),
                    line,
                    Component.NEW_LINE
            );
            complex.print(printStream);
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
