package com.lalaalal.mimo.logging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public abstract class Component {
    protected final List<Style> styles = new ArrayList<>();
    protected boolean useStyle;

    public static final Component SPACE = text(" ", false);
    public static final Component NEW_LINE = text("\n", false);

    public static Component text(String text) {
        return new TextComponent(text);
    }

    public static Component text(String text, boolean useStyle) {
        return new TextComponent(text).useStyle(useStyle);
    }

    public static Component withDefault(String text) {
        return text(text).with(Style.DEFAULT);
    }

    public static ComplexComponent complex(Component... components) {
        return new ComplexComponent(List.of(components));
    }

    public Component() {
        this(true);
    }

    public Component(boolean useStyle) {
        this.useStyle = useStyle;
    }

    public abstract void print(PrintStream printStream);

    public abstract List<Component> lines();

    protected void applyStyle() {
        if (useStyle)
            styles.forEach(Style::apply);
    }

    public Component with(Style style) {
        this.styles.add(style);
        return this;
    }

    public Component with(List<Style> styles) {
        this.styles.addAll(styles);
        return this;
    }

    public Component useStyle(boolean value) {
        this.useStyle = value;
        return this;
    }
}
