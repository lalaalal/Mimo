package com.lalaalal.mimo.console.command;

import com.lalaalal.mimo.console.argument.ArgumentParser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SimpleCommand<T> implements Command {
    private final Caster<T> caster;
    private final Function<List<String>, String> messageGenerator;
    private final List<String> helpComments;
    private final T action;

    public SimpleCommand(Caster<T> caster, Function<List<String>, String> messageGenerator, List<String> helpComments, T action) {
        this.caster = caster;
        this.messageGenerator = messageGenerator;
        this.helpComments = List.copyOf(helpComments);
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
    public List<String> help() {
        return helpComments;
    }

    public static class Builder<T> {
        private final Caster<T> caster;
        private final List<String> helpComments = new ArrayList<>();
        private Function<List<String>, String> messageGenerator = arguments -> "Done";
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
                    result = result.replaceFirst("\\{}", arguments.get(index));
                return result;
            };
            return this;
        }

        public Builder<T> help(String comment) {
            this.helpComments.add(comment);
            return this;
        }

        public Builder<T> help(ArgumentParser<?>... parsers) {
            return help(Command.argumentHelp(parsers));
        }

        public Command build() {
            return new SimpleCommand<>(caster, messageGenerator, helpComments, action);
        }
    }
}
