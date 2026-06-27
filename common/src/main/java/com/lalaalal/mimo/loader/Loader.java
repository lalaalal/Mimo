package com.lalaalal.mimo.loader;

import com.lalaalal.mimo.registry.RegistryItem;

import java.io.IOException;
import java.nio.file.Path;

public record Loader(Type type, String version, RegistryItem<ServerLauncher> launcher) {
    public Loader(String type, String version) {
        this(Type.get(type), version, ServerLauncher.FABRIC);
    }

    public Loader(String type, String version, RegistryItem<ServerLauncher> launcher) {
        this(Type.get(type), version, launcher);
    }

    public Process launch(String launcherFile, Path workingDirectory) throws IOException {
        return launcher.value().launch(launcherFile, workingDirectory);
    }

    @Override
    public String toString() {
        return type + " " + version;
    }

    public enum Type {
        DATAPACK, FABRIC, NEOFORGE, FORGE;

        public static Type get(String name) {
            for (Type value : values()) {
                if (value.name().equalsIgnoreCase(name))
                    return value;
            }
            return null;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
