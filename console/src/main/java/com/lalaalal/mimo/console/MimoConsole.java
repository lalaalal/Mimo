package com.lalaalal.mimo.console;

import com.lalaalal.mimo.ContentInstance;
import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.console.argument.ArgumentParsers;
import com.lalaalal.mimo.console.command.Command;
import com.lalaalal.mimo.console.command.Commands;
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

    public static void launchServer() throws IOException {
        ServerInstance serverInstance = Mimo.currentInstanceOrThrow();
        serverInstance.launch(System.out, System.in);
    }

    public static void main(String[] args) throws IOException {
        Mimo.initialize();
        ArgumentParsers.initialize();
        Commands.initialize();
        if (args.length > 0 && args[0].equals("debug"))
            Mimo.LOGGER.setLevel(Level.DEBUG);

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

    private static void runCommand(String commandString, String[] arguments) {
        Optional<Command> command = Commands.get(commandString);
        if (command.isPresent()) {
            Command.Result result = command.get().execute(arguments);
            for (String message : result.messages())
                Mimo.LOGGER.log(getLogLevel(result), message);
        } else {
            Mimo.LOGGER.error("Command %s not found".formatted(commandString));
        }
    }

    private static Level getLogLevel(Command.Result result) {
        if (result.succeed())
            return Level.INFO;
        return Level.ERROR;
    }
}