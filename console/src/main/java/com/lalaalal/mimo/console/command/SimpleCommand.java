package com.lalaalal.mimo.console.command;

import com.lalaalal.mimo.console.argument.ArgumentParser;
import com.lalaalal.mimo.exception.MessageComponentException;
import com.lalaalal.mimo.logging.ComplexMessageComponent;
import com.lalaalal.mimo.logging.MessageComponent;

import java.util.ArrayList;
import java.util.List;

public class SimpleCommand<T> implements Command {
    private final String name;
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
            ComplexMessageComponent component = new ComplexMessageComponent()
                    .addLine(exception.getMessageComponent());
            if (exception.shouldPrintHelp())
                Commands.help(this, false, component::addLine);
            return Result.fail(component);
        } catch (MessageComponentException exception) {
            return Result.fail(exception.getMessageComponent()
                    .complex()
                    .add(MessageComponent.NEW_LINE)
                    .add(MessageComponent.withDefault("Aborted"))
            );
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

        public Builder<T> formatHelp(Object... names) {
            helpComments.clear();
            StringBuilder builder = new StringBuilder(name).append(" ");
            for (Object name : names)
                builder.append("[").append(name).append("] ");
            return help(builder.toString());
        }

        public Builder<T> argumentHelp(String prefix, String name, ArgumentParser<?> argument) {
            return help(prefix + "[" + name + "] : " + argument.help());
        }

        public Builder<T> argumentHelp(String prefix, ArgumentParser<?> argument) {
            return help(prefix + "[" + argument.name + "] : " + argument.help());
        }

        public Command build() {
            return new SimpleCommand<>(name, caster, helpComments, action);
        }
    }
}
