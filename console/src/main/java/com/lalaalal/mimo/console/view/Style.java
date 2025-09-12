package com.lalaalal.mimo.console.view;

import java.util.Arrays;

@FunctionalInterface
public interface Style {
    String PATTERN = "\033[%sm";

    Style DEFAULT = () -> System.out.printf(PATTERN, "0");
    Style TITLE = of(DEFAULT, TextType.BOLD, TextType.UNDERLINE);

    static Style of(Style... styles) {
        return () -> Arrays.stream(styles)
                .forEach(Style::apply);
    }

    void apply();
}
