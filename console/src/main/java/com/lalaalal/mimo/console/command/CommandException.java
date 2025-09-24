package com.lalaalal.mimo.console.command;

import com.lalaalal.mimo.MessageComponentException;
import com.lalaalal.mimo.console.argument.ArgumentParser;
import com.lalaalal.mimo.logging.MessageComponent;

public class CommandException extends MessageComponentException {
    private final boolean shouldPrintHelp;

    public static CommandException missingArguments(ArgumentParser<?>... parsers) {
        StringBuilder builder = new StringBuilder();
        builder.append("Missing ").append(parsers.length).append(" argument(s):");
        for (ArgumentParser<?> parser : parsers)
            builder.append(" [").append(parser).append("]");
        return new CommandException(builder.toString(), true);
    }

    public CommandException(MessageComponent component, boolean shouldPrintHelp) {
        super(component);
        this.shouldPrintHelp = shouldPrintHelp;
    }

    public CommandException(String message, boolean shouldPrintHelp) {
        super(message);
        this.shouldPrintHelp = shouldPrintHelp;
    }

    public boolean shouldPrintHelp() {
        return shouldPrintHelp;
    }
}
