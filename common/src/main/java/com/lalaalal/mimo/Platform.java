package com.lalaalal.mimo;

import java.nio.file.Path;

public enum Platform {
    WINDOWS("AppData/Roaming/mimo"),
    MAC_OS(".local/share/mimo"),
    LINUX(".local/share/mimo");

    private static Platform instance;

    public static Platform get() {
        if (instance != null)
            return instance;
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win"))
            return instance = WINDOWS;
        if (osName.contains("mac"))
            return instance = MAC_OS;
        if (osName.contains("linux"))
            return instance = LINUX;
        throw new IllegalStateException("Unsupported operating system");
    }

    public final Path defaultMimoDirectory;

    Platform(String... more) {
        this.defaultMimoDirectory = Path.of(System.getProperty("user.home"), more);
    }
}
