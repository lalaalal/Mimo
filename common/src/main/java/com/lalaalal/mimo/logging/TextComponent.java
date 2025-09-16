package com.lalaalal.mimo.logging;

import java.io.PrintStream;

public class TextComponent extends Component {
    private final String text;

    public TextComponent(String text) {
        this.text = text;
    }

    @Override
    public void print(PrintStream printStream) {
        applyStyle();
        printStream.print(text);
    }
}
