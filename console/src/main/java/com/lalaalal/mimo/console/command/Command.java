package com.lalaalal.mimo.console.command;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.console.Registries;
import com.lalaalal.mimo.console.argument.ArgumentParser;
import com.lalaalal.mimo.console.argument.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Command {
    Result execute(List<String> arguments);

    List<String> help();

    default Result execute(String... arguments) {
        return execute(List.of(arguments));
    }

    default String name() {
        Mimo.LOGGER.warning("Calling default Command#name()");
        return Registries.COMMANDS.findKey(this);
    }

    static SimpleCommand.Builder<AC> simple(String name) {
        Caster<AC> caster = ((arguments, action) -> {
            verifyArgumentCount(arguments);
            action.accept();
        });
        return new SimpleCommand.Builder<>(name, caster).argumentHelp();
    }

    static <A> SimpleCommand.Builder<AC1<A>> simple(String name, final ArgumentParser<A> parser) {
        Caster<AC1<A>> caster = (arguments, consumer) -> {
            verifyArgumentCount(arguments, parser);
            A argument = parser.parse(arguments.getFirst());
            consumer.accept(argument);
        };
        return new SimpleCommand.Builder<>(name, caster).argumentHelp(parser);
    }

    static <A1, A2> SimpleCommand.Builder<AC2<A1, A2>> simple(String name, final ArgumentParser<A1> parser1, final ArgumentParser<A2> parser2) {
        Caster<AC2<A1, A2>> caster = (arguments, consumer) -> {
            verifyArgumentCount(arguments, parser1, parser2);
            A1 a1 = parser1.parse(arguments.getFirst());
            A2 a2 = parser2.parse(arguments.get(1));
            consumer.accept(a1, a2);
        };
        return new SimpleCommand.Builder<>(name, caster).argumentHelp(parser1, parser2);
    }

    static <A1, A2, A3> SimpleCommand.Builder<AC3<A1, A2, A3>> simple(String name, final ArgumentParser<A1> parser1, final ArgumentParser<A2> parser2, final ArgumentParser<A3> parser3) {
        Caster<AC3<A1, A2, A3>> caster = (arguments, consumer) -> {
            verifyArgumentCount(arguments, parser1, parser2, parser3);
            A1 a1 = parser1.parse(arguments.getFirst());
            A2 a2 = parser2.parse(arguments.get(1));
            A3 a3 = parser3.parse(arguments.get(2));
            consumer.accept(a1, a2, a3);
        };
        return new SimpleCommand.Builder<>(name, caster).argumentHelp(parser1, parser2, parser3);
    }

    static <A1, A2, A3, A4> SimpleCommand.Builder<AC4<A1, A2, A3, A4>> simple(String name, final ArgumentParser<A1> parser1, final ArgumentParser<A2> parser2, final ArgumentParser<A3> parser3, final ArgumentParser<A4> parser4) {
        Caster<AC4<A1, A2, A3, A4>> caster = (arguments, consumer) -> {
            verifyArgumentCount(arguments, parser1, parser2, parser3, parser4);
            A1 a1 = parser1.parse(arguments.getFirst());
            A2 a2 = parser2.parse(arguments.get(1));
            A3 a3 = parser3.parse(arguments.get(2));
            A4 a4 = parser4.parse(arguments.get(3));
            consumer.accept(a1, a2, a3, a4);
        };
        return new SimpleCommand.Builder<>(name, caster).argumentHelp(parser1, parser2, parser3, parser4);
    }

    static void verifyArgumentCount(List<String> arguments, ArgumentParser<?>... parsers) {
        int missingCount = parsers.length - arguments.size();
        if (missingCount < 0) {
            List<String> unused = arguments.subList(missingCount - 1, arguments.size());
            Mimo.LOGGER.warning("Ignored arguments %s".formatted(unused));
        }
        if (missingCount > 0) {
            ArgumentParser<?>[] missingParsers = Arrays.copyOfRange(parsers, parsers.length - missingCount, parsers.length);
            throw CommandException.missingArguments(missingParsers);
        }
    }

    static <T> SimpleCommand.Builder<AC1<List<T>>> list(String name, ArgumentParser<T> parser) {
        Caster<AC1<List<T>>> caster = (arguments, consumer) -> {
            List<T> list = new ArrayList<>();
            for (String argument : arguments) {
                list.add(parser.parse(argument));
            }
            consumer.accept(list);
        };
        return new SimpleCommand.Builder<>(name, caster)
                .argumentHelp("[" + parser.name + "...]");
    }

    static SimpleCommand.Builder<AC1<Arguments>> multiple(String name, ArgumentParser<?>... parsers) {
        Caster<AC1<Arguments>> caster = (arguments, consumer) -> {
            consumer.accept(new Arguments(arguments, List.of(parsers)));
        };
        return new SimpleCommand.Builder<>(name, caster);
    }

    static ComplexCommand.Builder complex(String name) {
        return new ComplexCommand.Builder(name);
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

    record Result(boolean succeed, List<String> messages) {
        public static Result success(String... messages) {
            return success(List.of(messages));
        }

        public static Result success(List<String> messages) {
            return new Result(true, messages);
        }

        public static Result fail(String... messages) {
            return fail(List.of(messages));
        }

        public static Result fail(List<String> messages) {
            return new Result(false, messages);
        }
    }
}
