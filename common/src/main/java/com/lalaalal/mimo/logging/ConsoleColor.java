package com.lalaalal.mimo.logging;

public enum ConsoleColor {
    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7),
    DEFAULT(9);

    final String code;

    ConsoleColor(String code) {
        this.code = code;
    }

    ConsoleColor(int code) {
        this(String.valueOf(code));
    }

    public ColorStyle foreground() {
        return new ColorStyle(ColorStyle.Target.FOREGROUND, this);
    }

    public ColorStyle background() {
        return new ColorStyle(ColorStyle.Target.BACKGROUND, this);
    }
}
