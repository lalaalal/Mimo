package com.lalaalal.mimo.console;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

public class MimoConsole {
    public static void listMods() {
        Mimo.currentInstance().ifPresent(
                serverInstance -> serverInstance.getContents().forEach(System.out::println)
        );
    }

    public static void launchServer() throws IOException, InterruptedException {
        Optional<ServerInstance> optional = Mimo.currentInstance();
        if (optional.isPresent())
            optional.get().launch(System.out, System.in);
    }

    public static void printHelp() {

    }

    public static void main(String[] args) throws IOException {
        Mimo.initialize();
        ArgumentParsers.initialize();
        Commands.initialize();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine();
                String[] tokens = line.split(" ");
                if (tokens[0].equals("exit"))
                    return;
                String[] arguments = Arrays.copyOfRange(tokens, 1, tokens.length);
                Commands.get(tokens[0]).ifPresentOrElse(command -> {
                    Command.Result result = command.execute(arguments);
                    if (!result.message().isBlank())
                        System.out.println(result.message());
                }, MimoConsole::printHelp);
            }
        }
    }
}