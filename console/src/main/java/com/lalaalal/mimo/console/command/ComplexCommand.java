package com.lalaalal.mimo.console.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComplexCommand implements Command {
    private final Map<Integer, Command> overloadCommands;
    private final Map<String, Command> subCommands;
    private final List<String> helpComments;

    public ComplexCommand(Map<Integer, Command> overloadCommands, Map<String, Command> subCommands) {
        this.overloadCommands = Map.copyOf(overloadCommands);
        this.subCommands = Map.copyOf(subCommands);
        this.helpComments = new ArrayList<>();
        for (Command command : overloadCommands.values())
            helpComments.addAll(command.help());
        this.subCommands.forEach((name, command) -> {
            List<String> help = command.help()
                    .stream()
                    .map(value -> " " + name + value)
                    .toList();
            helpComments.addAll(help);
        });
    }

    @Override
    public Result execute(List<String> arguments) {
        if (!arguments.isEmpty()) {
            String subCommand = arguments.getFirst();
            if (subCommands.containsKey(subCommand)) {
                Command command = subCommands.get(subCommand);
                return command.execute(arguments.subList(1, arguments.size()));
            }
        }
        Command command = overloadCommands.get(arguments.size());
        return command.execute(arguments);
    }

    @Override
    public List<String> help() {
        return helpComments;
    }

    public static final class Builder {
        private final Map<Integer, Command> overloadCommands = new HashMap<>();
        private final Map<String, Command> subCommands = new HashMap<>();

        public Builder overload(int argc, Command command) {
            overloadCommands.put(argc, command);
            return this;
        }

        public Builder subcommand(String name, Command command) {
            subCommands.put(name, command);
            return this;
        }

        public ComplexCommand build() {
            return new ComplexCommand(overloadCommands, subCommands);
        }
    }
}
