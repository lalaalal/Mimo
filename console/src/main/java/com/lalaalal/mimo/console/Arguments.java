package com.lalaalal.mimo.console;

import java.util.List;

public class Arguments {
    private final List<String> arguments;
    private final List<ArgumentParser<?>> parsers;

    public Arguments(List<String> arguments, List<ArgumentParser<?>> parsers) {
        if (arguments.size() != parsers.size())
            throw new IllegalArgumentException("Arguments and parsers must have the same size");
        this.arguments = arguments;
        this.parsers = parsers;
    }

    public <T> T get(int index, Class<T> type) {
        String argument = arguments.get(index);
        ArgumentParser<T> parser = ArgumentParser.cast(parsers.get(index), type);

        return parser.parse(argument);
    }
}
