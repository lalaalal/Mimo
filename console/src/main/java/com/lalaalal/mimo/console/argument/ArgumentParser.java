package com.lalaalal.mimo.console.argument;

import java.util.function.Function;

public abstract class ArgumentParser<T> {
    public final String name;
    public final Class<T> type;

    public static <T> ArgumentParser<T> of(String name, Class<T> type, Function<String, T> parser) {
        return new ArgumentParser<>(name, type) {
            @Override
            public T parse(String value) {
                T result = parser.apply(value);
                if (result == null)
                    throw new IllegalArgumentException("Cannot parse " + value + " to " + type.getSimpleName());
                return result;
            }
        };
    }

    public ArgumentParser(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    public abstract T parse(String value);

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
}
