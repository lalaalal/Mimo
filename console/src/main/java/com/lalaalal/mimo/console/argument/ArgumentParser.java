package com.lalaalal.mimo.console.argument;

import java.util.function.Function;

public abstract class ArgumentParser<T> {
    public final String name;
    public final String helpMessage;
    public final Class<T> type;

    public static <T> Builder<T> builder(String name, Class<T> type) {
        return new Builder<>(name, type);
    }

    public static <T> Builder<T> builder(String name, Class<T> type, Function<String, T> parser) {
        return new Builder<>(name, type).parser(parser);
    }

    public static <T> ArgumentParser<T> of(String name, Class<T> type, Function<String, T> parser) {
        return builder(name, type, parser).build();
    }

    public ArgumentParser(String name, String helpMessage, Class<T> type) {
        this.name = name;
        this.helpMessage = helpMessage;
        this.type = type;
    }

    public abstract T parse(String value);

    public String help() {
        return helpMessage;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @SuppressWarnings("unchecked")
    public static <T> ArgumentParser<T> cast(ArgumentParser<?> parser, Class<T> type) {
        if (parser.type == type)
            return (ArgumentParser<T>) parser;
        throw new IllegalArgumentException("Cannot cast " + parser.type + " to " + type);
    }

    public static class Builder<T> {
        private final String name;
        private final Class<T> type;
        private String helpMessage;
        private Function<String, T> parser;

        public Builder(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }

        public Builder<T> help(String helpMessage) {
            this.helpMessage = helpMessage;
            return this;
        }

        public Builder<T> parser(Function<String, T> parser) {
            this.parser = parser;
            return this;
        }

        public ArgumentParser<T> build() {
            if (helpMessage == null)
                helpMessage = name;
            return new ArgumentParser<>(name, helpMessage, type) {
                @Override
                public T parse(String value) {
                    return parser.apply(value);
                }
            };
        }
    }
}
