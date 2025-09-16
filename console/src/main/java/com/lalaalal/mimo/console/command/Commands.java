package com.lalaalal.mimo.console.command;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.console.MimoConsole;
import com.lalaalal.mimo.console.Registries;
import com.lalaalal.mimo.console.argument.ArgumentParsers;

import java.util.Optional;

@SuppressWarnings("unused")
public class Commands {
    public static Command register(Command command) {
        return Registries.COMMANDS.register(command.name(), command);
    }

    public static Optional<Command> get(String name) {
        return Optional.ofNullable(Registries.COMMANDS.get(name));
    }

    public static final Command INSTALL = register(
            Command.simple("install",
                            ArgumentParsers.LOADER_TYPE,
                            ArgumentParsers.STRING,
                            ArgumentParsers.MINECRAFT_VERSION,
                            ArgumentParsers.STRING)
                    .argumentHelp(
                            ArgumentParsers.LOADER_TYPE,
                            "name",
                            ArgumentParsers.MINECRAFT_VERSION,
                            "loader_version")
                    .action(Mimo::install)
                    .build()
    );

    public static final Command LOAD = register(
            Command.simple("load", ArgumentParsers.STRING)
                    .action(Mimo::load)
                    .build()
    );

    public static final Command ADD = register(
            Command.complex("add")
                    .overload(1, Command.simple("add_only", ArgumentParsers.STRING)
                            .argumentHelp("name")
                            .action(Mimo::add)
                            .build())
                    .overload(2, Command.simple("add",
                                    ArgumentParsers.STRING, ArgumentParsers.BOOLEAN)
                            .argumentHelp("name", "update")
                            .help(" -    [name]  : " + ArgumentParsers.STRING)
                            .help(" -    [update]: " + ArgumentParsers.BOOLEAN)
                            .action(Mimo::add)
                            .build())
                    .build()
    );

    public static final Command REMOVE = register(
            Command.simple("remove", ArgumentParsers.STRING)
                    .action(Mimo::remove)
                    .build()
    );

    public static final Command UPDATE = register(
            Command.simple("update")
                    .action(Mimo::update)
                    .build()
    );

    public static final Command LIST = register(
            Command.complex("list")
                    .subCommand("server", Command.simple("server")
                            .action(MimoConsole::listServers)
                            .build())
                    .subCommand("version", Command.simple("loader_version",
                                    ArgumentParsers.LOADER_TYPE,
                                    ArgumentParsers.MINECRAFT_VERSION,
                                    ArgumentParsers.INTEGER)
                            .argumentHelp(ArgumentParsers.LOADER_TYPE,
                                    ArgumentParsers.MINECRAFT_VERSION,
                                    "limit")
                            .action(MimoConsole::listLoaderVersions)
                            .build())
                    .overload(0, Command.simple("list_all")
                            .action(MimoConsole::list)
                            .build())
                    .overload(1, Command.simple("list_by_type", ArgumentParsers.PROJECT_TYPE)
                            .action(MimoConsole::list)
                            .build())
                    .build()
    );

    public static final Command LAUNCH = register(
            Command.simple("launch")
                    .action(MimoConsole::launchServer)
                    .build()
    );

    public static final Command HELP = register(
            Command.complex("help")
                    .overload(0, Command.simple("help_all")
                            .action(Commands::help)
                            .build())
                    .overload(1, Command.simple("help_single", ArgumentParsers.STRING)
                            .action(Commands::help)
                            .build())
                    .build()
    );

    public static void initialize() {

    }

    private static void help() {
        for (String commandKey : Registries.COMMANDS.keySet())
            help(commandKey, true);
    }

    private static void help(String commandKey) {
        help(commandKey, false);
    }

    private static void help(String commandKey, boolean onlyArgument) {
        if (!Registries.COMMANDS.contains(commandKey))
            throw new IllegalArgumentException("Command \"%s\" not found".formatted(commandKey));
        Command command = Registries.COMMANDS.get(commandKey);
        for (String help : command.help()) {
            if (onlyArgument && help.startsWith(" -"))
                continue;
            Mimo.LOGGER.info(commandKey + help);
        }
    }
}
