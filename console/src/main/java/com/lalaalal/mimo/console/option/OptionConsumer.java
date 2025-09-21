package com.lalaalal.mimo.console.option;

import java.util.ArrayList;
import java.util.List;

public class OptionConsumer {
    private final List<String> tokens;

    public OptionConsumer(String... tokens) {
        this(List.of(tokens));
    }

    public OptionConsumer(List<String> tokens) {
        this.tokens = new ArrayList<>(tokens);
    }

    public boolean isOption(String token) {
        return token.startsWith("-");
    }

    public boolean hasMore() {
        return !this.tokens.isEmpty();
    }

    public boolean hasOption() {
        return hasMore() && isOption(this.tokens.getFirst());
    }

    public String first() {
        if (this.tokens.isEmpty())
            throw new IllegalStateException("No more option to consume");
        return this.tokens.getFirst();
    }

    public String consume() {
        if (this.tokens.isEmpty())
            throw new IllegalStateException("No more option to consume");
        return this.tokens.removeFirst();
    }

    public List<String> consumeOptionArguments() {
        List<String> arguments = new ArrayList<>();
        while (hasMore() && !isOption(first()))
            arguments.add(consume());

        return List.copyOf(arguments);
    }
}
