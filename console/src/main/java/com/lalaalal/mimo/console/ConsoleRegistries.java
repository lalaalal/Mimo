package com.lalaalal.mimo.console;

import com.lalaalal.mimo.Registry;
import com.lalaalal.mimo.console.argument.ArgumentParser;
import com.lalaalal.mimo.console.command.Command;

public class ConsoleRegistries {
    public static final Registry<Command> COMMANDS = Registry.create("commands");
    public static final Registry<ArgumentParser<?>> ARGUMENT_PARSERS = Registry.create("argument_parsers");

    public static final Registry<Command> OPTIONS = Registry.create("options");
}
