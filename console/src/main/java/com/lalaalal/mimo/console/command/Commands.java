package com.lalaalal.mimo.console.command;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.console.ConsoleRegistries;
import com.lalaalal.mimo.console.MimoConsole;
import com.lalaalal.mimo.console.argument.ArgumentParsers;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Commands {
    public static Command register(Command command) {
        return ConsoleRegistries.COMMANDS.register(command.name(), command);
    }

    public static Optional<Command> get(String name) {
        return Optional.ofNullable(ConsoleRegistries.COMMANDS.get(name));
    }

    public static final Command INSTALL = register(
            Command.complex("install")
                    .overload(4, Command.simple("install",
                                    ArgumentParsers.LOADER_TYPE,
                                    ArgumentParsers.STRING,
                                    ArgumentParsers.MINECRAFT_VERSION,
                                    ArgumentParsers.STRING)
                            .formatHelp(ArgumentParsers.LOADER_TYPE, "server_name", ArgumentParsers.MINECRAFT_VERSION, "loader_version")
                            .argumentHelp("-    ", ArgumentParsers.LOADER_TYPE)
                            .help("-    Install server")
                            .action(Mimo::install)
                            .build())
                    .overload(3, Command.simple("install",
                                    ArgumentParsers.LOADER_TYPE,
                                    ArgumentParsers.STRING,
                                    ArgumentParsers.MINECRAFT_VERSION)
                            .formatHelp(ArgumentParsers.LOADER_TYPE, "server_name", ArgumentParsers.MINECRAFT_VERSION)
                            .argumentHelp("-    ", ArgumentParsers.LOADER_TYPE)
                            .help("-    Install server with latest loader version")
                            .help("+    version [loader_type] [minecraft_version]")
                            .action(Mimo::install)
                            .build())
                    .build()
    );

    public static final Command LOAD = register(
            Command.simple("load", ArgumentParsers.STRING)
                    .formatHelp("server_name")
                    .action(Mimo::load)
                    .build()
    );

    public static final Command RESCAN = register(
            Command.simple("rescan", ArgumentParsers.STRING)
                    .formatHelp("server_name")
                    .action(Mimo::rescan)
                    .build()
    );

    public static final Command SEARCH = register(
            Command.simple("search", ArgumentParsers.STRING, ArgumentParsers.CONTENT_PROVIDER)
                    .argumentHelp("-    ", ArgumentParsers.CONTENT_PROVIDER)
                    .action(MimoConsole::search)
                    .build()
    );

    public static final Command ADD = register(
            Command.complex("add")
                    .overload(2, Command.simple("add", ArgumentParsers.STRING, ArgumentParsers.CONTENT_PROVIDER)
                            .formatHelp("content_slug", ArgumentParsers.CONTENT_PROVIDER)
                            .action(Mimo::add)
                            .build())
                    .overload(3, Command.simple("add",
                                    ArgumentParsers.STRING, ArgumentParsers.CONTENT_PROVIDER, ArgumentParsers.BOOLEAN)
                            .formatHelp("content_slug", ArgumentParsers.CONTENT_PROVIDER, "update")
                            .argumentHelp("-    ", ArgumentParsers.CONTENT_PROVIDER)
                            .argumentHelp("-    ", "update", ArgumentParsers.BOOLEAN)
                            .action(Mimo::add)
                            .build())
                    .build()
    );

    public static final Command REMOVE = register(
            Command.complex("remove")
                    .subCommand("content", Command.simple("remove content", ArgumentParsers.STRING)
                            .formatHelp("content_slug")
                            .help("-    Remove content")
                            .help("-    Server load required")
                            .help("-    Auto save")
                            .action(Mimo::removeContent)
                            .build())
                    .subCommand("server", Command.simple("remove server", ArgumentParsers.STRING)
                            .formatHelp("server_name")
                            .help("-    Remove server")
                            .action(Mimo::removeServer)
                            .build())
                    .build()
    );

    public static final Command UPDATE = register(
            Command.simple("update")
                    .help("-    Download and update all contents")
                    .help("-    Server load required")
                    .action(Mimo::update)
                    .build()
    );

    public static final Command SAVE = register(
            Command.simple("save")
                    .help("-    Save server")
                    .help("-    Server load required")
                    .action(Mimo::save)
                    .build()
    );

    public static final Command LIST = register(
            Command.complex("list")
                    .subCommand("server", Command.simple("list server")
                            .help("-    List existing servers")
                            .action(MimoConsole::listServers)
                            .build())
                    .overload(0, Command.simple("list")
                            .help("-    List all contents")
                            .help("-    Server load required")
                            .action(MimoConsole::list)
                            .build())
                    .overload(1, Command.simple("list", ArgumentParsers.PROJECT_TYPE)
                            .argumentHelp("-    ", ArgumentParsers.PROJECT_TYPE)
                            .action(MimoConsole::list)
                            .build())
                    .build()
    );

    public static final Command VERSION = register(
            Command.complex("version")
                    .overload(0, Command.simple("version")
                            .help("-    Get Mimo version")
                            .action(() -> Mimo.LOGGER.info("Mimo " + MimoConsole.VERSION))
                            .build())
                    .overload(2, Command.simple("version",
                                    ArgumentParsers.LOADER_TYPE,
                                    ArgumentParsers.MINECRAFT_VERSION)
                            .argumentHelp("-    ", ArgumentParsers.LOADER_TYPE)
                            .help("-    Get latest loader version")
                            .action(MimoConsole::listLoaderVersion)
                            .build())
                    .overload(3, Command.simple("version",
                                    ArgumentParsers.LOADER_TYPE,
                                    ArgumentParsers.MINECRAFT_VERSION,
                                    ArgumentParsers.INTEGER)
                            .formatHelp(ArgumentParsers.LOADER_TYPE, ArgumentParsers.MINECRAFT_VERSION, "limit")
                            .argumentHelp("-    ", ArgumentParsers.LOADER_TYPE)
                            .argumentHelp("-    ", "limit", ArgumentParsers.INTEGER)
                            .action(MimoConsole::listLoaderVersions)
                            .build())
                    .build()
    );

    public static final Command LAUNCH = register(
            Command.complex("launch")
                    .overload(0, Command.simple("launch")
                            .help("-    Launch loaded server")
                            .help("-    Server load required")
                            .action(MimoConsole::launchServer)
                            .build())
                    .overload(1, Command.simple("launch", ArgumentParsers.STRING)
                            .help("-    Load and launch server")
                            .formatHelp("server_name")
                            .action(MimoConsole::launchServer)
                            .build())
                    .build()
    );

    public static final Command LOG = register(
            Command.simple("log", ArgumentParsers.LOG_LEVEL)
                    .action(Mimo.LOGGER::setLevel)
                    .build()
    );

    public static final Command HELP = register(
            Command.complex("help")
                    .overload(0, Command.simple("help")
                            .action(Commands::help)
                            .build())
                    .overload(1, Command.list("help", ArgumentParsers.STRING)
                            .action(Commands::help)
                            .formatHelp("command...")
                            .build())
                    .build()
    );

    public static void initialize() {

    }

    private static void help() {
        for (String commandKey : ConsoleRegistries.COMMANDS.keySet())
            help(commandKey, true);
    }

    public static void help(String commandKey) {
        help(commandKey, false);
    }

    public static void help(String commandKey, boolean onlyArgument) {
        if (!ConsoleRegistries.COMMANDS.contains(commandKey))
            throw CommandException.notFound(commandKey);
        Command command = ConsoleRegistries.COMMANDS.get(commandKey);
        help(command, onlyArgument, Mimo.LOGGER::info);
    }

    public static void help(List<String> commands) {
        String commandKey = commands.getFirst();
        if (!ConsoleRegistries.COMMANDS.contains(commandKey))
            throw CommandException.notFound(commandKey);
        Command command = ConsoleRegistries.COMMANDS.get(commandKey);
        for (String subCommandKey : commands.subList(1, commands.size())) {
            Optional<Command> subCommand = command.resolve(subCommandKey);
            if (subCommand.isPresent())
                command = subCommand.get();
        }
        help(command, false, Mimo.LOGGER::info);
    }

    public static void help(Command command, boolean onlyArgument, Consumer<String> consumer) {
        for (String help : command.help()) {
            if (help.matches("^[-+].*$") && onlyArgument)
                continue;
            consumer.accept(help);
        }
    }
}
