package com.lalaalal.mimo.console.command;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.console.MimoConsole;
import com.lalaalal.mimo.console.Registries;
import com.lalaalal.mimo.console.argument.ArgumentParsers;

import java.util.Optional;

public class Commands {
    public static Command register(String name, Command command) {
        return Registries.COMMANDS.register(name, command);
    }

    public static Optional<Command> get(String name) {
        return Optional.ofNullable(Registries.COMMANDS.get(name));
    }

    public static final Command INSTALL = register("install",
            Command.simple(ArgumentParsers.LOADER_TYPE, ArgumentParsers.STRING, ArgumentParsers.MINECRAFT_VERSION, ArgumentParsers.STRING)
                    .action(Mimo::install)
                    .message("Minecraft {} with loader {} installed.", 2, 0)
                    .build()
    );

    public static final Command LOAD = register("load",
            Command.simple(ArgumentParsers.STRING)
                    .action(Mimo::load)
                    .build()
    );

    public static final Command ADD = register("add",
            Command.simple(ArgumentParsers.STRING)
                    .action(Mimo::add)
                    .build()
    );

    public static final Command REMOVE = register("remove",
            Command.simple(ArgumentParsers.STRING)
                    .action(Mimo::remove)
                    .build()
    );

    public static final Command UPDATE = register("update",
            Command.simple()
                    .action(Mimo::update)
                    .build()
    );

    public static final Command LIST = register("list",
            Command.complex()
                    .subcommand("server", Command.simple()
                            .action(MimoConsole::listServers)
                            .message("")
                            .build())
                    .overload(0, Command.simple()
                            .action(MimoConsole::list)
                            .message("")
                            .build())
                    .overload(1, Command.simple(ArgumentParsers.PROJECT_TYPE)
                            .action(MimoConsole::list)
                            .message("")
                            .build())
                    .build()
    );

    public static final Command LAUNCH = register("launch",
            Command.simple()
                    .action(MimoConsole::launchServer)
                    .build()
    );

    public static final Command HELP = register("help",
            Command.complex()
                    .overload(0, Command.simple()
                            .action(Commands::help)
                            .message("")
                            .build())
                    .overload(1, Command.simple(ArgumentParsers.STRING)
                            .action(Commands::help)
                            .message("")
                            .build())
                    .build()
    );

    public static void initialize() {

    }

    private static void help() {
        for (String commandKey : Registries.COMMANDS.keySet())
            help(commandKey);
    }

    private static void help(String commandKey) {
        if (!Registries.COMMANDS.contains(commandKey))
            throw new IllegalArgumentException("Command \"%s\" not found".formatted(commandKey));
        Command command = Registries.COMMANDS.get(commandKey);
        for (String help : command.help())
            Mimo.LOGGER.info(commandKey + help);
    }
}
