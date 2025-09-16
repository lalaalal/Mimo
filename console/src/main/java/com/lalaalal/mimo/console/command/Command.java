package com.lalaalal.mimo.console.command;

import com.lalaalal.mimo.console.Registries;
import com.lalaalal.mimo.console.argument.ArgumentParser;
import com.lalaalal.mimo.console.argument.Arguments;

import java.util.List;

public interface Command {
    Result execute(List<String> arguments);

    List<String> help();

    default Result execute(String... arguments) {
        return execute(List.of(arguments));
    }

    static SimpleCommand.Builder<AC> simple() {
        Caster<AC> caster = ((arguments, action) -> action.accept());
        return new SimpleCommand.Builder<>(caster).help();
    }

    static <A> SimpleCommand.Builder<AC1<A>> simple(final ArgumentParser<A> parser) {
        Caster<AC1<A>> caster = (arguments, consumer) -> {
            if (arguments.isEmpty())
                throw new IllegalArgumentException("Missing argument");
            A argument = parser.parse(arguments.getFirst());
            consumer.accept(argument);
        };
        return new SimpleCommand.Builder<>(caster).help(parser);
    }

    static <A1, A2> SimpleCommand.Builder<AC2<A1, A2>> simple(final ArgumentParser<A1> parser1, final ArgumentParser<A2> parser2) {
        Caster<AC2<A1, A2>> caster = (arguments, consumer) -> {
            if (arguments.size() < 2)
                throw new IllegalArgumentException("Missing argument");
            A1 a1 = parser1.parse(arguments.getFirst());
            A2 a2 = parser2.parse(arguments.get(1));
            consumer.accept(a1, a2);
        };
        return new SimpleCommand.Builder<>(caster).help(parser1, parser2);
    }

    static <A1, A2, A3> SimpleCommand.Builder<AC3<A1, A2, A3>> simple(final ArgumentParser<A1> parser1, final ArgumentParser<A2> parser2, final ArgumentParser<A3> parser3) {
        Caster<AC3<A1, A2, A3>> caster = (arguments, consumer) -> {
            if (arguments.size() < 3)
                throw new IllegalArgumentException("Missing argument");
            A1 a1 = parser1.parse(arguments.getFirst());
            A2 a2 = parser2.parse(arguments.get(1));
            A3 a3 = parser3.parse(arguments.get(2));
            consumer.accept(a1, a2, a3);
        };
        return new SimpleCommand.Builder<>(caster).help(parser1, parser2, parser3);
    }

    static <A1, A2, A3, A4> SimpleCommand.Builder<AC4<A1, A2, A3, A4>> simple(final ArgumentParser<A1> parser1, final ArgumentParser<A2> parser2, final ArgumentParser<A3> parser3, final ArgumentParser<A4> parser4) {
        Caster<AC4<A1, A2, A3, A4>> caster = (arguments, consumer) -> {
            if (arguments.size() < 4)
                throw new IllegalArgumentException("Missing argument");
            A1 a1 = parser1.parse(arguments.getFirst());
            A2 a2 = parser2.parse(arguments.get(1));
            A3 a3 = parser3.parse(arguments.get(2));
            A4 a4 = parser4.parse(arguments.get(3));
            consumer.accept(a1, a2, a3, a4);
        };
        return new SimpleCommand.Builder<>(caster).help(parser1, parser2, parser3, parser4);
    }

    static SimpleCommand.Builder<AC1<Arguments>> simple(ArgumentParser<?>... parsers) {
        Caster<AC1<Arguments>> caster = (arguments, consumer) -> {
            consumer.accept(new Arguments(arguments, List.of(parsers)));
        };
        return new SimpleCommand.Builder<>(caster).help(parsers);
    }

    static String argumentHelp(ArgumentParser<?>... parsers) {
        StringBuilder builder = new StringBuilder();
        for (ArgumentParser<?> parser : parsers) {
            String parserName = Registries.ARGUMENT_PARSERS.findKey(parser);
            builder.append(" [").append(parserName).append("]");
        }
        return builder.toString();
    }

    static ComplexCommand.Builder complex() {
        return new ComplexCommand.Builder();
    }

    interface Caster<T> {
        void executeAction(List<String> arguments, T action) throws Throwable;
    }

    interface AC {
        void accept() throws Throwable;
    }

    interface AC1<A> {
        void accept(A a) throws Throwable;
    }

    interface AC2<A1, A2> {
        void accept(A1 a1, A2 a2) throws Throwable;
    }

    interface AC3<A1, A2, A3> {
        void accept(A1 a1, A2 a2, A3 a3) throws Throwable;
    }

    interface AC4<A1, A2, A3, A4> {
        void accept(A1 a1, A2 a2, A3 a3, A4 a4) throws Throwable;
    }

    record Result(boolean succeed, String message) {
        public static Result success(String message) {
            return new Result(true, message);
        }

        public static Result fail(String message) {
            return new Result(false, message);
        }
    }
}
