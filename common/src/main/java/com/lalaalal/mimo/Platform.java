package com.lalaalal.mimo;

import java.nio.file.Path;

public enum Platform {
    MAC_OS(".local/share/mimo"),
    LINUX(".local/share/mimo");

    private static Platform INSTANCE;

    public static Platform get() {
        if (INSTANCE != null)
            return INSTANCE;
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac"))
            return INSTANCE = MAC_OS;
        if (osName.contains("linux"))
            return INSTANCE = LINUX;
        throw new IllegalStateException("Unsupported operating system");
    }

    public final Path defaultMimoDirectory;

    Platform(String... more) {
        this.defaultMimoDirectory = Path.of(System.getProperty("user.home"), more);
    }
}
