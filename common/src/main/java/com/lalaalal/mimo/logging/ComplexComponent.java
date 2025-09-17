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

    @Override
    public List<Component> lines() {
        List<Component> result = new ArrayList<>();
        ComplexComponent current = new ComplexComponent();
        current.with(this.styles).useStyle(this.useStyle);
        result.add(current);
        for (Component component : components) {
            List<Component> childLines = component.lines();
            for (int index = 0; index < childLines.size(); index++) {
                if (index > 0) {
                    current = new ComplexComponent();
                    current.with(this.styles).useStyle(this.useStyle);
                    result.add(current);
                }
                current.add(childLines.get(index));
            }
        }
        return result;
    }
}
