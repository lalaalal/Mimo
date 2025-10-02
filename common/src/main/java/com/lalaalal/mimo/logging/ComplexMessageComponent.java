package com.lalaalal.mimo.logging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ComplexMessageComponent extends MessageComponent {
    private final List<MessageComponent> components = new ArrayList<>();

    public ComplexMessageComponent() {

    }

    public ComplexMessageComponent(List<MessageComponent> components) {
        this.components.addAll(components);
    }

    public ComplexMessageComponent insert(MessageComponent component) {
        this.components.addFirst(component.useStyle(useStyle));
        return this;
    }

    public ComplexMessageComponent insertLine(MessageComponent component) {
        return insert(component).add(MessageComponent.NEW_LINE);
    }

    public ComplexMessageComponent add(MessageComponent component) {
        this.components.add(component.useStyle(useStyle));
        return this;
    }

    public ComplexMessageComponent add(List<MessageComponent> components) {
        components.forEach(this::add);
        return this;
    }

    public ComplexMessageComponent add(String text) {
        return add(MessageComponent.text(text, useStyle).with(styles));
    }

    public ComplexMessageComponent addLine(MessageComponent component) {
        return add(component).add(MessageComponent.NEW_LINE);
    }

    public ComplexMessageComponent addLine(String text) {
        return add(text).add(MessageComponent.NEW_LINE);
    }

    @Override
    public void print(PrintStream printStream) {
        applyStyle();
        components.forEach(component -> component.print(printStream));
    }

    @Override
    public String plainText() {
        StringBuilder builder = new StringBuilder();
        components.stream()
                .map(MessageComponent::plainText)
                .forEach(builder::append);
        return builder.toString();
    }

    @Override
    public List<MessageComponent> lines() {
        List<MessageComponent> result = new ArrayList<>();
        result.add(new ComplexMessageComponent());
        for (MessageComponent component : components) {
            List<MessageComponent> lines = component.lines();
            if (lines.isEmpty()) {
                result.add(new ComplexMessageComponent());
                continue;
            }
            ComplexMessageComponent last = result.removeLast().complex();
            last.add(lines.getFirst());
            result.add(last);
            result.addAll(lines.subList(1, lines.size()));
        }
        return result;
    }

    @Override
    public MessageComponent useStyle(boolean value) {
        for (MessageComponent component : components)
            component.useStyle(value);
        return super.useStyle(value);
    }

    @Override
    public ComplexMessageComponent complex() {
        return this;
    }
}
