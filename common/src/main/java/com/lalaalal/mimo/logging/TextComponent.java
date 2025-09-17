package com.lalaalal.mimo.logging;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

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

    @Override
    public List<Component> lines() {
        return Arrays.stream(text.split("\n"))
                .map(line -> Component.text(line, this.useStyle)
                        .with(this.styles))
                .toList();
    }
}
