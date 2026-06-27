package com.lalaalal.mimo;

import java.nio.file.Path;

public enum Platform {
    WINDOWS("AppData/Roaming/mimo", ".bat"),
    MAC_OS(".local/share/mimo", ".sh"),
    LINUX(".local/share/mimo", ".sh");

    private static Platform instance;

    public static Platform current() {
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
    public final String scriptExtension;

    Platform(String mimoPath, String scriptExtension) {
        this.defaultMimoDirectory = Path.of(System.getProperty("user.home"), mimoPath);
        this.scriptExtension = scriptExtension;
    }
}
