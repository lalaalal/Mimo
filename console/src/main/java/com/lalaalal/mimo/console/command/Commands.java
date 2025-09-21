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
            Command.complex("install")
                    .overload(4, Command.simple("install",
                                    ArgumentParsers.LOADER_TYPE,
                                    ArgumentParsers.STRING,
                                    ArgumentParsers.MINECRAFT_VERSION,
                                    ArgumentParsers.STRING)
                            .argumentHelp(
                                    ArgumentParsers.LOADER_TYPE,
                                    "server_name",
                                    ArgumentParsers.MINECRAFT_VERSION,
                                    "loader_version")
                            .help("-    Install server")
                            .help("+    version [loader_type] [minecraft_version]")
                            .action(Mimo::install)
                            .build())
                    .overload(3, Command.simple("install_latest",
                                    ArgumentParsers.LOADER_TYPE,
                                    ArgumentParsers.STRING,
                                    ArgumentParsers.MINECRAFT_VERSION)
                            .argumentHelp(
                                    ArgumentParsers.LOADER_TYPE,
                                    "server_name",
                                    ArgumentParsers.MINECRAFT_VERSION)
                            .help("-    Install server with latest loader version")
                            .help("+    version [loader_type] [minecraft_version]")
                            .action(Mimo::install)
                            .build())
                    .build()
    );

    public static final Command LOAD = register(
            Command.simple("load", ArgumentParsers.STRING)
                    .argumentHelp("server_name")
                    .action(Mimo::load)
                    .build()
    );

    public static final Command ADD = register(
            Command.complex("add")
                    .overload(1, Command.simple("add_only", ArgumentParsers.STRING)
                            .argumentHelp("content_slug")
                            .action(Mimo::add)
                            .build())
                    .overload(2, Command.simple("add",
                                    ArgumentParsers.STRING, ArgumentParsers.BOOLEAN)
                            .argumentHelp("content_slug", "update")
                            .help("-    [content_slug] : " + ArgumentParsers.STRING)
                            .help("-    [update]       : " + ArgumentParsers.BOOLEAN)
                            .action(Mimo::add)
                            .build())
                    .subCommand("all", Command.list("add_all", ArgumentParsers.STRING)
                            .argumentHelp("content_slug...")
                            .action(Mimo::add)
                            .build())
                    .build()
    );

    public static final Command REMOVE = register(
            Command.complex("remove")
                    .subCommand("content", Command.simple("remove_content", ArgumentParsers.STRING)
                            .argumentHelp("content_name")
                            .help("-    Remove content")
                            .help("-    Required server load first")
                            .action(Mimo::removeContent)
                            .build())
                    .subCommand("server", Command.simple("remove_server", ArgumentParsers.STRING)
                            .argumentHelp("server_name")
                            .help("-    Remove server")
                            .action(Mimo::removeServer)
                            .build())
                    .build()
    );

    public static final Command UPDATE = register(
            Command.simple("update")
                    .help("-    Download and update all contents")
                    .help("-    Required server load first")
                    .action(Mimo::update)
                    .build()
    );

    public static final Command LIST = register(
            Command.complex("list")
                    .subCommand("server", Command.simple("server")
                            .action(MimoConsole::listServers)
                            .build())
                    .overload(0, Command.simple("list_all")
                            .help("-    List all contents")
                            .help("-    Required server load first")
                            .action(MimoConsole::list)
                            .build())
                    .overload(1, Command.simple("list_by_type", ArgumentParsers.PROJECT_TYPE)
                            .help("-    [project_type] : [mod|datapack]")
                            .action(MimoConsole::list)
                            .build())
                    .build()
    );

    public static final Command VERSION = register(
            Command.complex("version")
                    .overload(2, Command.simple("version_latest",
                                    ArgumentParsers.LOADER_TYPE,
                                    ArgumentParsers.MINECRAFT_VERSION)
                            .help("-    Get latest loader version")
                            .action(MimoConsole::listLoaderVersion)
                            .build())
                    .overload(3, Command.simple("version",
                                    ArgumentParsers.LOADER_TYPE,
                                    ArgumentParsers.MINECRAFT_VERSION,
                                    ArgumentParsers.INTEGER)
                            .argumentHelp(ArgumentParsers.LOADER_TYPE,
                                    ArgumentParsers.MINECRAFT_VERSION,
                                    "limit")
                            .action(MimoConsole::listLoaderVersions)
                            .build())
                    .build()
    );

    public static final Command LAUNCH = register(
            Command.complex("launch")
                    .overload(0, Command.simple("launch")
                            .help("-    Launch loaded server")
                            .help("-    Required server load first")
                            .action(MimoConsole::launchServer)
                            .build())
                    .overload(1, Command.simple("launch", ArgumentParsers.STRING)
                            .help("-    Load and launch server")
                            .argumentHelp("server_name")
                            .action(MimoConsole::launchServer)
                            .build())
                    .build()
    );

    public static final Command HELP = register(
            Command.complex("help")
                    .overload(0, Command.simple("help_all")
                            .action(Commands::help)
                            .build())
                    .overload(1, Command.simple("help_single", ArgumentParsers.STRING)
                            .action(Commands::help)
                            .argumentHelp("command")
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
            if (help.matches("^[-+].*$")) {
                if (onlyArgument)
                    continue;
                Mimo.LOGGER.info(help);
            } else {
                Mimo.LOGGER.info(commandKey + " " + help);
            }
        }
    }
}
