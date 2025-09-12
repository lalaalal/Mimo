package com.lalaalal.mimo.console;

import java.util.List;
import java.util.function.Function;

public interface Command {
    Result execute(List<String> arguments);

    String help();

    default Result execute(String... arguments) {
        return execute(List.of(arguments));
    }

    class Simple<T> implements Command {
        private final Caster<T> caster;
        private final Function<List<String>, String> messageGenerator;
        private final String helpComment;
        private final T action;

        public Simple(Caster<T> caster, Function<List<String>, String> messageGenerator, String helpComment, T action) {
            this.caster = caster;
            this.messageGenerator = messageGenerator;
            this.helpComment = helpComment;
            this.action = action;
        }

        @Override
        public Result execute(List<String> arguments) {
            try {
                caster.executeAction(arguments, action);
                return Result.success(messageGenerator.apply(arguments));
            } catch (Throwable throwable) {
                return Result.fail(throwable.getMessage());
            }
        }

        @Override
        public String help() {
            return helpComment;
        }
    }

    static Builder<AC> simple() {
        Caster<AC> caster = ((arguments, action) -> action.accept());
        return new Builder<>(caster);
    }

    static <A> Builder<AC1<A>> simple(final ArgumentParser<A> parser) {
        Caster<AC1<A>> caster = (arguments, consumer) -> {
            A argument = parser.parse(arguments.getFirst());
            consumer.accept(argument);
        };
        return new Builder<>(caster).help(parser);
    }

    static <A1, A2> Builder<AC2<A1, A2>> simple(final ArgumentParser<A1> parser1, final ArgumentParser<A2> parser2) {
        Caster<AC2<A1, A2>> caster = (arguments, consumer) -> {
            A1 a1 = parser1.parse(arguments.getFirst());
            A2 a2 = parser2.parse(arguments.get(1));
            consumer.accept(a1, a2);
        };
        return new Builder<>(caster).help(parser1, parser2);
    }

    static <A1, A2, A3> Builder<AC3<A1, A2, A3>> simple(final ArgumentParser<A1> parser1, final ArgumentParser<A2> parser2, final ArgumentParser<A3> parser3) {
        Caster<AC3<A1, A2, A3>> caster = (arguments, consumer) -> {
            A1 a1 = parser1.parse(arguments.getFirst());
            A2 a2 = parser2.parse(arguments.get(1));
            A3 a3 = parser3.parse(arguments.get(2));
            consumer.accept(a1, a2, a3);
        };
        return new Builder<>(caster).help(parser1, parser2, parser3);
    }

    static <A1, A2, A3, A4> Builder<AC4<A1, A2, A3, A4>> simple(final ArgumentParser<A1> parser1, final ArgumentParser<A2> parser2, final ArgumentParser<A3> parser3, final ArgumentParser<A4> parser4) {
        Caster<AC4<A1, A2, A3, A4>> caster = (arguments, consumer) -> {
            A1 a1 = parser1.parse(arguments.getFirst());
            A2 a2 = parser2.parse(arguments.get(1));
            A3 a3 = parser3.parse(arguments.get(2));
            A4 a4 = parser4.parse(arguments.get(3));
            consumer.accept(a1, a2, a3, a4);
        };
        return new Builder<>(caster).help(parser1, parser2, parser3, parser4);
    }

    static Builder<AC1<Arguments>> complex(ArgumentParser<?>... parsers) {
        Caster<AC1<Arguments>> caster = (arguments, consumer) -> {
            consumer.accept(new Arguments(arguments, List.of(parsers)));
        };
        return new Builder<>(caster).help(parsers);
    }

    static String argumentHelp(ArgumentParser<?>... parsers) {
        StringBuilder builder = new StringBuilder();
        for (ArgumentParser<?> parser : parsers) {
            String parserName = Registries.ARGUMENT_PARSERS.findKey(parser);
            builder.append(" [").append(parserName).append("]");
        }
        return builder.append('\n').toString();
    }

    class Builder<T> {
        private final Caster<T> caster;
        private Function<List<String>, String> messageGenerator = arguments -> "Done";
        private String helpComment = "";
        private T action;

        public Builder(Caster<T> caster) {
            this.caster = caster;
        }

        public Builder<T> action(T method) {
            this.action = method;
            return this;
        }

        public Builder<T> message(Function<List<String>, String> messageGenerator) {
            this.messageGenerator = messageGenerator;
            return this;
        }

        public Builder<T> message(String message) {
            this.messageGenerator = arguments -> message;
            return this;
        }

        public Builder<T> message(String message, int... replaceIndexList) {
            this.messageGenerator = arguments -> {
                String result = message;
                for (int index : replaceIndexList)
                    result = message.replaceFirst("\\{}", arguments.get(index));
                return result;
            };
            return this;
        }

        public Builder<T> help(String comment) {
            this.helpComment = comment;
            return this;
        }

        public Builder<T> appendHelp(String comment) {
            this.helpComment += comment;
            return this;
        }

        public Builder<T> help(ArgumentParser<?>... parsers) {
            return help(argumentHelp(parsers));
        }

        public Command build() {
            return new Simple<>(caster, messageGenerator, helpComment, action);
        }
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
        public static Result success() {
            return new Result(true, null);
        }

        public static Result success(String message) {
            return new Result(true, message);
        }

        public static Result fail(String message) {
            return new Result(false, message);
        }
    }
}
