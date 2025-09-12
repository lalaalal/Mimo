package com.lalaalal.mimo.console.view;

public enum TextType implements Style {
    RESET(0),
    BOLD(1),
    ITALIC(3),
    UNDERLINE(4),
    INVERT(7),
    REMOVE_BOLD(21),
    REMOVE_ITALIC(23),
    REMOVE_UNDERLINE(24),
    REMOVE_INVERT(27);

    private final String code;

    TextType(int code) {
        this.code = String.valueOf(code);
    }

    @Override
    public void apply() {
        System.out.printf(PATTERN, code);
    }
}
