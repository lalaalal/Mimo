package com.lalaalal.mimo.loader;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.data.MinecraftVersion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LoaderInstaller {
    private static final Map<Loader.Type, LoaderInstaller> INSTALLERS = new HashMap<>();
    protected static final Path INSTANCES_PATH = Mimo.getInstanceContainerDirectory();

    public final Loader.Type loaderType;

    public static void initialize() {
        INSTALLERS.put(Loader.Type.FABRIC, create(Loader.Type.FABRIC));
    }

    public static LoaderInstaller get(Loader.Type loader) {
        return INSTALLERS.computeIfAbsent(loader, LoaderInstaller::create);
    }

    private static LoaderInstaller create(Loader.Type loader) {
        try {
            return switch (loader) {
                case FABRIC -> new FabricInstaller();
                case NEOFORGE -> throw new UnsupportedOperationException("Not implemented");
            };
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    protected LoaderInstaller(Loader.Type loaderType) {
        this.loaderType = loaderType;
    }

    public abstract List<String> getAvailableVersions(MinecraftVersion minecraftVersion);

    public abstract boolean isValidVersion(MinecraftVersion minecraftVersion, String loaderVersion);

    protected Path createInstanceDirectory(String name) throws IOException {
        Path instanceDirectory = INSTANCES_PATH.resolve(name);
        if (Files.exists(instanceDirectory))
            throw new IllegalStateException("Instance %s already exists".formatted(name));
        return Files.createDirectories(instanceDirectory);
    }

    public abstract ServerInstance install(String name, MinecraftVersion minecraftVersion, String loaderVersion) throws IOException;
}
