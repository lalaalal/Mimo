package com.lalaalal.mimo.logging;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TextMessageComponent extends MessageComponent {
    private final String text;

    public TextMessageComponent(String text) {
        this.text = Objects.requireNonNullElse(text, "");
    }

    @Override
    public void print(PrintStream printStream) {
        applyStyle();
        printStream.print(text);
    }

    @Override
    public String plainText() {
        return text;
    }

    @Override
    public List<MessageComponent> lines() {
        return Arrays.stream(text.split("\n"))
                .map(line -> MessageComponent.text(line, this.useStyle)
                        .with(this.styles))
                .toList();
    }

    @Override
    public ComplexMessageComponent complex() {
        return MessageComponent.complex(this);
    }
}
