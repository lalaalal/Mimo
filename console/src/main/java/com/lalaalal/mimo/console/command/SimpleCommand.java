package com.lalaalal.mimo.console.command;

import java.util.ArrayList;
import java.util.List;

public class SimpleCommand<T> implements Command {
    public final String name;
    private final Caster<T> caster;
    private final List<String> argumentHelp;
    private final T action;

    public SimpleCommand(String name, Caster<T> caster, List<String> helpComments, T action) {
        this.name = name;
        this.caster = caster;
        this.argumentHelp = List.copyOf(helpComments);
        this.action = action;
    }

    @Override
    public Result execute(List<String> arguments) {
        try {
            caster.executeAction(arguments, action);
            return Result.success("Done");
        } catch (CommandException exception) {
            List<String> comments = new ArrayList<>(exception.getMessages());
            if (exception.shouldPrintHelp())
                help().forEach(comment -> comments.add(name() + comment));
            return Result.fail(comments);
        } catch (Throwable throwable) {
            return Result.fail(throwable.getMessage());
        }
    }

    @Override
    public List<String> help() {
        return argumentHelp;
    }

    @Override
    public String name() {
        return this.name;
    }

    public static class Builder<T> {
        private final String name;
        private final Caster<T> caster;
        private final List<String> helpComments = new ArrayList<>();
        private T action;

        public Builder(String name, Caster<T> caster) {
            this.name = name;
            this.caster = caster;
        }

        public Builder<T> action(T method) {
            this.action = method;
            return this;
        }

        public Builder<T> help(boolean append, String comment) {
            if (!append)
                this.helpComments.clear();
            this.helpComments.add(comment);
            return this;
        }

        public Builder<T> help(String comment) {
            return help(true, comment);
        }

        public Builder<T> argumentHelp(boolean append, Object... argumentNames) {
            if (!append)
                this.helpComments.clear();
            StringBuilder builder = new StringBuilder();
            for (Object argumentName : argumentNames)
                builder.append(" [").append(argumentName).append("]");
            return help(append, builder.toString());
        }

        public Builder<T> argumentHelp(Object... argumentNames) {
            return argumentHelp(false, argumentNames);
        }

        public Command build() {
            return new SimpleCommand<>(name, caster, helpComments, action);
        }
    }
}
