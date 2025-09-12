package com.lalaalal.mimo.console.view;

import org.junit.jupiter.api.Test;

class ComponentTest {
    @Test
    void text() {
        Component.of("Red underline\n")
                .with(ConsoleColor.RED.foreground())
                .with(ConsoleColor.GREEN.background())
                .with(TextType.UNDERLINE)
                .print();
    }

    @Test
    void complex() {
        Component a = Component.withDefault("Red underline")
                .with(ConsoleColor.RED.foreground())
                .with(ConsoleColor.GREEN.background())
                .with(TextType.UNDERLINE);
        Component b = Component.withDefault("Blue italic")
                .with(ConsoleColor.BLUE.foreground())
                .with(TextType.ITALIC);
        new ComplexComponent()
                .add(a)
                .add(b)
                .print();
    }
}