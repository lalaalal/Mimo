package com.lalaalal.mimo.console.view;

import java.util.ArrayList;
import java.util.List;

public class ComplexComponent extends Component {
    private final List<Component> components = new ArrayList<>();

    public ComplexComponent add(Component component) {
        this.components.add(component);
        return this;
    }

    @Override
    public void print() {
        applyStyle();
        components.forEach(Component::print);
    }
}
