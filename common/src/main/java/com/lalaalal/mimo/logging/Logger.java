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

    private MessageComponent getTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        return MessageComponent.text(String.format("[%02d:%02d:%02d]", now.getHour(), now.getMinute(), now.getSecond()), useStyle)
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

    public void log(Level level, MessageComponent message) {
        if (this.level.shouldLog(level))
            message.lines().forEach(line -> internalLog(level, line));
    }

    private void internalLog(Level level, MessageComponent line) {
        MessageComponent complex = MessageComponent.complex(
                getTimestamp(), MessageComponent.SPACE,
                MessageComponent.text("[" + getCaller() + "]", useStyle)
                        .with(ConsoleColor.CYAN.foreground()), MessageComponent.SPACE,
                MessageComponent.text("[" + level.name() + "]", useStyle)
                        .with(level.style),
                MessageComponent.text(": ", useStyle)
                        .with(Style.DEFAULT),
                line.useStyle(useStyle),
                MessageComponent.NEW_LINE
        );
        complex.print(printStream);
    }

    public void log(Level level, String message, Object... args) {
        if (this.level.shouldLog(level)) {
            for (Object arg : args)
                message = message.replaceFirst("\\{}", arg.toString());
            log(level, MessageComponent.withDefault(message));
        }
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void verbose(String message, Object... args) {
        log(Level.VERBOSE, message, args);
    }

    public void debug(String message, Object... args) {
        log(Level.DEBUG, message, args);
    }

    public void info(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    public void warning(String message, Object... args) {
        log(Level.WARNING, message, args);
    }

    public void error(String message, Object... args) {
        log(Level.ERROR, message, args);
    }

    public void info(MessageComponent message) {
        log(Level.INFO, message);
    }

    public void warning(MessageComponent message) {
        log(Level.WARNING, message);
    }

    public void error(MessageComponent message) {
        log(Level.ERROR, message);
    }
}
