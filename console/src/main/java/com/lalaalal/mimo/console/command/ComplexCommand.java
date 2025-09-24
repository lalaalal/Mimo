package com.lalaalal.mimo.console.command;

import com.lalaalal.mimo.logging.ComplexMessageComponent;

import java.util.*;

public class ComplexCommand implements Command {
    private final String name;
    private final Map<Integer, Command> overloadCommands;
    private final Map<String, Command> subCommands;
    private final List<String> helpComments;
    private final int minArgumentCount;
    private final int maxArgumentCount;

    private ComplexCommand(String name, Map<Integer, Command> overloadCommands, Map<String, Command> subCommands, int minArgumentCount, int maxArgumentCount) {
        this.name = name;
        this.overloadCommands = Map.copyOf(overloadCommands);
        this.subCommands = Map.copyOf(subCommands);
        this.helpComments = new ArrayList<>();
        for (Command command : overloadCommands.values())
            helpComments.addAll(command.help());
        subCommands.forEach((subName, command) -> {
            Commands.help(command, false, helpComments::add);
        });

        this.minArgumentCount = minArgumentCount;
        this.maxArgumentCount = maxArgumentCount;
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
        if (overloadCommands.isEmpty()) {
            ComplexMessageComponent component = new ComplexMessageComponent()
                    .addLine("No matching command : %s %s".formatted(name(), arguments));
            Commands.help(this, false, component::addLine);
            return Result.fail(component);
        }
        int argumentCount = select(arguments);
        return overloadCommands.get(argumentCount)
                .execute(arguments);
    }

    private int select(List<String> arguments) {
        int argumentCount = arguments.size();
        if (overloadCommands.containsKey(argumentCount))
            return argumentCount;
        if (argumentCount < minArgumentCount)
            return minArgumentCount;
        return maxArgumentCount;
    }

    @Override
    public Optional<Command> resolve(String child) {
        return Optional.ofNullable(subCommands.get(child));
    }

    @Override
    public List<String> help() {
        return helpComments;
    }

    @Override
    public String name() {
        return name;
    }

    public static final class Builder {
        private final String name;
        private final Map<Integer, Command> overloadCommands = new HashMap<>();
        private final Map<String, Command> subCommands = new HashMap<>();
        private int minArgumentCount = Integer.MAX_VALUE;
        private int maxArgumentCount = Integer.MIN_VALUE;

        public Builder(String name) {
            this.name = name;
        }

        public Builder overload(int argc, Command command) {
            overloadCommands.put(argc, command);
            minArgumentCount = Math.min(minArgumentCount, argc);
            maxArgumentCount = Math.max(maxArgumentCount, argc);
            return this;
        }

        public Builder subCommand(String name, Command command) {
            subCommands.put(name, command);
            return this;
        }

        public ComplexCommand build() {
            return new ComplexCommand(name, overloadCommands, subCommands, minArgumentCount, maxArgumentCount);
        }
    }
}
