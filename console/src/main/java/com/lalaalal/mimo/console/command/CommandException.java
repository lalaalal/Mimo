package com.lalaalal.mimo.console.command;

import com.lalaalal.mimo.console.argument.ArgumentParser;
import com.lalaalal.mimo.exception.MessageComponentException;
import com.lalaalal.mimo.logging.ComplexMessageComponent;
import com.lalaalal.mimo.logging.ConsoleColor;
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

    public static CommandException notFound(String commandKey) {
        MessageComponent message = new ComplexMessageComponent()
                .add(MessageComponent.withDefault("Command "))
                .add(MessageComponent.withDefault(commandKey).with(ConsoleColor.RED.foreground()))
                .add(MessageComponent.withDefault(" not found"));
        return new CommandException(message, false);
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
