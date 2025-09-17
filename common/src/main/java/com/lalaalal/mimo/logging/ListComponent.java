package com.lalaalal.mimo.logging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListComponent extends Component {
    private final List<String> texts;
    private final boolean ordered;

    public static <T> ListComponent ordered(List<T> list) {
        return new ListComponent(list.stream().map(Object::toString).toList(), true);
    }

    @SafeVarargs
    public static <T> ListComponent ordered(T... list) {
        return ordered(Arrays.asList(list));
    }

    public static <T> ListComponent unordered(List<T> list) {
        return new ListComponent(list.stream().map(Object::toString).toList(), false);
    }

    @SafeVarargs
    public static <T> ListComponent unordered(T... list) {
        return unordered(Arrays.asList(list));
    }

    public ListComponent(List<String> texts, boolean ordered) {
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
    public List<Component> lines() {
        List<Component> result = new ArrayList<>();
        for (String text : texts) {
            Arrays.stream(text.split("\n"))
                    .map(line -> Component.text(line, this.useStyle)
                            .with(this.styles))
                    .forEach(result::add);
        }
        return result;
    }

    protected String getHead(int index) {
        return ordered ? "%02d ".formatted(index) : "- ";
    }
}
