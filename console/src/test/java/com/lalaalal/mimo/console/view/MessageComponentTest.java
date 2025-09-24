package com.lalaalal.mimo.console.view;

import com.lalaalal.mimo.logging.ComplexMessageComponent;
import com.lalaalal.mimo.logging.ConsoleColor;
import com.lalaalal.mimo.logging.MessageComponent;
import com.lalaalal.mimo.logging.TextType;
import org.junit.jupiter.api.Test;

class MessageComponentTest {
    @Test
    void text() {
        MessageComponent.text("Red underline\n")
                .with(ConsoleColor.RED.foreground())
                .with(ConsoleColor.GREEN.background())
                .with(TextType.UNDERLINE)
                .print(System.out);
    }

    @Test
    void complex() {
        MessageComponent a = MessageComponent.withDefault("Red underline")
                .with(ConsoleColor.RED.foreground())
                .with(ConsoleColor.GREEN.background())
                .with(TextType.UNDERLINE);
        MessageComponent b = MessageComponent.withDefault("Blue italic")
                .with(ConsoleColor.BLUE.foreground())
                .with(TextType.ITALIC);
        new ComplexMessageComponent()
                .add(a)
                .add(b)
                .print(System.out);
    }
}