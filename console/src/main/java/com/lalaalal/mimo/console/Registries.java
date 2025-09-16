package com.lalaalal.mimo.console;

import com.lalaalal.mimo.console.argument.ArgumentParser;
import com.lalaalal.mimo.console.command.Command;

public class Registries {
    public static final Registry<Command> COMMANDS = Registry.create("commands");
    public static final Registry<ArgumentParser<?>> ARGUMENT_PARSERS = Registry.create("argument_parsers");
}
