package com.lalaalal.mimo.console.command;

import com.lalaalal.mimo.console.argument.ArgumentParser;

import java.util.List;

public class CommandException extends RuntimeException {
    private final List<String> messages;
    private final boolean shouldPrintHelp;

    private static String pickFirstMessage(List<String> messages) {
        if (messages.isEmpty())
            return "";
        return messages.getFirst();
    }

    public static CommandException missingArguments(ArgumentParser<?>... parsers) {
        StringBuilder builder = new StringBuilder();
        builder.append("Missing ").append(parsers.length).append(" argument(s):");
        for (ArgumentParser<?> parser : parsers)
            builder.append(" [").append(parser).append("]");
        return new CommandException(true, builder.toString());
    }

    public static CommandException executionFailed(String... messages) {
        return new CommandException(false, messages);
    }

    protected CommandException(boolean shouldPrintHelp, List<String> messages) {
        super(pickFirstMessage(messages));
        this.shouldPrintHelp = shouldPrintHelp;
        this.messages = messages;
    }

    protected CommandException(boolean shouldPrintHelp, String... messages) {
        this(shouldPrintHelp, List.of(messages));
    }

    public List<String> getMessages() {
        return messages;
    }

    public boolean shouldPrintHelp() {
        return shouldPrintHelp;
    }
}
