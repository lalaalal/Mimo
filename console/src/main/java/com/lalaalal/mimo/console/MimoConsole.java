package com.lalaalal.mimo.console;

import com.lalaalal.mimo.ContentInstance;
import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.console.argument.ArgumentParsers;
import com.lalaalal.mimo.console.command.Command;
import com.lalaalal.mimo.console.command.Commands;
import com.lalaalal.mimo.console.option.OptionConsumer;
import com.lalaalal.mimo.console.option.Options;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.data.ProjectType;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.loader.LoaderInstaller;
import com.lalaalal.mimo.logging.Level;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class MimoConsole {
    public static void list() {
        for (ContentInstance content : Mimo.currentInstanceOrThrow().getContents())
            Mimo.LOGGER.info(content.getStyledText());
    }

    public static void list(ProjectType type) {
        for (ContentInstance content : Mimo.currentInstanceOrThrow().getContents()) {
            if (content.content().type() == type)
                Mimo.LOGGER.info(content.getStyledText());
        }
    }

    public static void listLoaderVersion(Loader.Type type, MinecraftVersion minecraftVersion) {
        listLoaderVersions(type, minecraftVersion, 1);
    }

    public static void listLoaderVersions(Loader.Type type, MinecraftVersion minecraftVersion, final int limit) {
        List<String> loaderVersions = LoaderInstaller.get(type)
                .getAvailableVersions(minecraftVersion);
        int index = 0;
        for (; index < limit && index < loaderVersions.size(); index++)
            Mimo.LOGGER.info(loaderVersions.get(index));
        Mimo.LOGGER.info("Total : %d".formatted(index));
    }

    public static void listServers() {
        String[] servers = Mimo.getServers();
        for (String serverName : servers)
            Mimo.LOGGER.info(serverName);
        Mimo.LOGGER.info("Total : %d".formatted(servers.length));
    }

    public static void launchServer() throws IOException, InterruptedException {
        ServerInstance serverInstance = Mimo.currentInstanceOrThrow();
        serverInstance.launch();
    }

    public static void launchServer(String serverName) throws IOException, InterruptedException {
        ServerInstance serverInstance = Mimo.load(serverName);
        serverInstance.launch();
    }

    public static void runCommand(String commandString, String... arguments) {
        runCommand(commandString, List.of(arguments));
    }

    public static void runCommand(String commandString, List<String> arguments) {
        Optional<Command> command = Commands.get(commandString);
        if (command.isPresent()) {
            Command.Result result = command.get().execute(arguments);
            Mimo.LOGGER.log(getLogLevel(result), result.message());
        } else {
            Mimo.LOGGER.error("Command \"%s\" not found".formatted(commandString));
        }
    }

    private static Level getLogLevel(Command.Result result) {
        if (result.succeed())
            return Level.INFO;
        return Level.ERROR;
    }

    public static void initialize() throws IOException {
        Mimo.initialize();
        ArgumentParsers.initialize();
        Commands.initialize();
        Options.initialize();
    }

    public static void handleOptions(String[] args) {
        OptionConsumer optionConsumer = new OptionConsumer(args);
        while (optionConsumer.hasOption()) {
            String optionString = optionConsumer.consume();
            List<String> arguments = optionConsumer.consumeOptionArguments();
            Options.get(optionString).ifPresent(option -> {
                option.execute(arguments);
            });
        }
    }

    public static void main(String[] args) throws IOException {
        initialize();
        handleOptions(args);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine();
                String[] tokens = line.split(" ");
                if (tokens[0].equals("exit"))
                    return;
                String[] arguments = Arrays.copyOfRange(tokens, 1, tokens.length);
                runCommand(tokens[0], arguments);
            }
        }
    }
}