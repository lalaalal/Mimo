package com.lalaalal.mimo.logging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ComplexComponent extends Component {
    private final List<Component> components = new ArrayList<>();

    public ComplexComponent() {

    }

    public ComplexComponent(List<Component> components) {
        this.components.addAll(components);
    }

    public ComplexComponent add(Component component) {
        this.components.add(component);
        return this;
    }

    public ComplexComponent add(List<Component> components) {
        this.components.addAll(components);
        return this;
    }

    @Override
    public void print(PrintStream printStream) {
        applyStyle();
        components.forEach(component -> component.print(printStream));
    }
}
