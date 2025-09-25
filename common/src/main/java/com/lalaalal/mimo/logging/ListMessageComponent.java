package com.lalaalal.mimo.logging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListMessageComponent extends MessageComponent {
    private final List<String> texts;
    private final boolean ordered;

    public static <T> ListMessageComponent ordered(List<T> list) {
        return new ListMessageComponent(list.stream().map(Object::toString).toList(), true);
    }

    @SafeVarargs
    public static <T> ListMessageComponent ordered(T... list) {
        return ordered(Arrays.asList(list));
    }

    public static <T> ListMessageComponent unordered(List<T> list) {
        return new ListMessageComponent(list.stream().map(Object::toString).toList(), false);
    }

    @SafeVarargs
    public static <T> ListMessageComponent unordered(T... list) {
        return unordered(Arrays.asList(list));
    }

    public ListMessageComponent(List<String> texts, boolean ordered) {
        this.texts = new ArrayList<>();
        this.ordered = ordered;
        for (int index = 0; index < texts.size(); index++)
            this.texts.add(getHead(index + 1) + texts.get(index));
    }

    @Override
    public void print(PrintStream printStream) {
        applyStyle();
        texts.forEach(printStream::println);
    }

    @Override
    public String plainText() {
        return String.join("\n", texts);
    }

    @Override
    public List<MessageComponent> lines() {
        List<MessageComponent> result = new ArrayList<>();
        for (String text : texts) {
            Arrays.stream(text.split("\n"))
                    .map(line -> MessageComponent.text(line, this.useStyle)
                            .with(this.styles))
                    .forEach(result::add);
        }
        return result;
    }

    @Override
    public ComplexMessageComponent complex() {
        return MessageComponent.complex(this);
    }

    protected String getHead(int index) {
        return ordered ? "%02d ".formatted(index) : "- ";
    }
}
