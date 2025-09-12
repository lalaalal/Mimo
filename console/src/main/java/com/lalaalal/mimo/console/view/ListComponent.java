package com.lalaalal.mimo.console.view;

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
        return ordered(Arrays.asList(list));
    }

    public ListComponent(List<String> texts, boolean ordered) {
        this.texts = texts;
        this.ordered = ordered;
    }

    @Override
    public void print() {
        applyStyle();
        for (int index = 0; index < texts.size(); index++)
            System.out.println(getHead(index) + texts.get(index));
    }

    protected String getHead(int index) {
        return ordered ? "%02d ".formatted(index) : "- ";
    }
}
