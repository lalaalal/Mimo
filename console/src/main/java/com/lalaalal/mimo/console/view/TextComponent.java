package com.lalaalal.mimo.console.view;

public class TextComponent extends Component {
    private final String text;

    public TextComponent(String text) {
        this.text = text;
    }

    @Override
    public void print() {
        applyStyle();
        System.out.print(text);
    }
}
