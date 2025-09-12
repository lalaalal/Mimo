package com.lalaalal.mimo.console.view;

import java.util.ArrayList;
import java.util.List;

public abstract class Component {
    private final List<Style> styles = new ArrayList<>();

    static Component of(String text) {
        return new TextComponent(text);
    }

    static Component withDefault(String text) {
        return of(text).with(Style.DEFAULT);
    }

    public abstract void print();

    protected void applyStyle() {
        styles.forEach(Style::apply);
    }

    public Component with(Style style) {
        this.styles.add(style);
        return this;
    }
}
