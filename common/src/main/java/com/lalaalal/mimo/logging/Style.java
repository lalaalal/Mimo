package com.lalaalal.mimo.logging;

import java.util.Arrays;

@FunctionalInterface
public interface Style {
    String PATTERN = "\033[%sm";

    Style NOTHING = () -> {
    };
    Style DEFAULT = () -> System.out.printf(PATTERN, "0");

    static Style of(Style... styles) {
        return () -> Arrays.stream(styles)
                .forEach(Style::apply);
    }

    void apply();
}
