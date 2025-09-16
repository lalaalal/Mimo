package com.lalaalal.mimo.logging;

public record ColorStyle(Target target, ConsoleColor color) implements Style {
    @Override
    public void apply() {
        System.out.printf(PATTERN, target.code + color.code);
    }

    public enum Target {
        FOREGROUND("3"), BACKGROUND("4");

        private final String code;

        Target(String code) {
            this.code = code;
        }
    }
}
