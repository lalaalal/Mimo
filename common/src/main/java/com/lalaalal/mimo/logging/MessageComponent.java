package com.lalaalal.mimo.logging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public abstract class MessageComponent {
    protected final List<Style> styles = new ArrayList<>();
    protected boolean useStyle;

    public static final MessageComponent SPACE = text(" ", false);
    public static final MessageComponent NEW_LINE = text("\n", false);

    public static MessageComponent text(String text) {
        return new TextMessageComponent(text);
    }

    public static MessageComponent text(String text, boolean useStyle) {
        return new TextMessageComponent(text).useStyle(useStyle);
    }

    public static MessageComponent withDefault(String text) {
        return text(text).with(Style.DEFAULT);
    }

    public static ComplexMessageComponent complex(MessageComponent... components) {
        return new ComplexMessageComponent(List.of(components));
    }

    public MessageComponent() {
        this(true);
    }

    public MessageComponent(boolean useStyle) {
        this.useStyle = useStyle;
    }

    public abstract void print(PrintStream printStream);

    public abstract String plainText();

    public abstract List<MessageComponent> lines();

    protected void applyStyle() {
        if (useStyle)
            styles.forEach(Style::apply);
    }

    public MessageComponent with(Style style) {
        this.styles.add(style);
        return this;
    }

    public MessageComponent with(List<Style> styles) {
        this.styles.addAll(styles);
        return this;
    }

    public MessageComponent useStyle(boolean value) {
        this.useStyle = value;
        return this;
    }
}
