package com.lalaalal.mimo.logging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public abstract class Component {
    private final List<Style> styles = new ArrayList<>();
    private boolean useStyle;

    public static final Component SPACE = of(" ");
    public static final Component NEW_LINE = of("\n");

    public static Component of(String text) {
        return new TextComponent(text);
    }

    public static Component of(String text, boolean useStyle) {
        return new TextComponent(text).useStyle(useStyle);
    }

    public static Component withDefault(String text) {
        return of(text).with(Style.DEFAULT);
    }

    public static Component complex(Component... components) {
        return new ComplexComponent(List.of(components));
    }

    public Component() {
        this(true);
    }

    public Component(boolean useStyle) {
        this.useStyle = useStyle;
    }

    public abstract void print(PrintStream printStream);

    protected void applyStyle() {
        if (useStyle)
            styles.forEach(Style::apply);
    }

    public Component with(Style style) {
        this.styles.add(style);
        return this;
    }

    public Component useStyle(boolean value) {
        this.useStyle = value;
        return this;
    }
}
