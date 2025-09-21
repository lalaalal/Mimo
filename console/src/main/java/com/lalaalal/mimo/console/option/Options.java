package com.lalaalal.mimo.console.option;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.console.MimoConsole;
import com.lalaalal.mimo.console.Registries;
import com.lalaalal.mimo.console.argument.ArgumentParsers;
import com.lalaalal.mimo.console.command.Command;
import com.lalaalal.mimo.logging.Level;

import java.util.List;
import java.util.Optional;

public class Options {
    public static Optional<Command> get(String name) {
        return Optional.ofNullable(Registries.OPTIONS.get(name));
    }

    public static Command register(String name, String alias, Command command) {
        Registries.OPTIONS.register(name, command);
        Registries.OPTIONS.register(alias, command);
        return command;
    }

    public static final Command DEBUG = register("--debug", "-d",
            Command.simple("debug")
                    .action(() -> Mimo.LOGGER.setLevel(Level.DEBUG))
                    .build()
    );

    public static final Command VERBOSE = register("--verbose", "-v",
            Command.simple("verbose")
                    .action(() -> Mimo.LOGGER.setLevel(Level.VERBOSE))
                    .build()
    );

    public static final Command COMMAND = register("--command", "-c",
            Command.list("command", ArgumentParsers.STRING)
                    .action(Options::executeCommand)
                    .build()
    );

    private static void executeCommand(List<String> tokens) {
        String commandString = tokens.getFirst();
        List<String> arguments = tokens.subList(1, tokens.size());
        MimoConsole.runCommand(commandString, arguments);
        System.exit(0);
    }

    public static void initialize() {

    }
}
