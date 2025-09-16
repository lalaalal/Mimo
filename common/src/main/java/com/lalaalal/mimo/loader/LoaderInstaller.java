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
    private static final String FILE_NAME_FORMAT = "%s-server-%s+%s.jar";
    private static final Map<Loader.Type, LoaderInstaller> INSTALLERS = new HashMap<>();
    protected static final Path INSTANCES_PATH = Mimo.getInstanceContainerDirectory();

    public final Loader.Type loaderType;

    public static void initialize() throws IOException {
        INSTALLERS.put(Loader.Type.FABRIC, new FabricInstaller());
    }

    public static LoaderInstaller get(Loader.Type loader) {
        return INSTALLERS.computeIfAbsent(loader, LoaderInstaller::create);
    }

    private static LoaderInstaller create(Loader.Type loader) {
        try {
            return switch (loader) {
                case FABRIC -> new FabricInstaller();
                case NEOFORGE -> throw new UnsupportedOperationException("Not implemented");
                default -> throw new IllegalArgumentException("Unsupported loader type");
            };
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    protected LoaderInstaller(Loader.Type loaderType) {
        this.loaderType = loaderType;
    }

    public String getFileName(MinecraftVersion minecraftVersion, String loaderVersion) {
        return FILE_NAME_FORMAT.formatted(loaderType, loaderVersion, minecraftVersion);
    }

    public abstract List<String> getAvailableVersions(MinecraftVersion minecraftVersion);

    public abstract boolean isValidVersion(MinecraftVersion minecraftVersion, String loaderVersion);

    protected Path createInstanceDirectory(String name) throws IOException {
        Path instanceDirectory = INSTANCES_PATH.resolve(name);
        if (Files.exists(instanceDirectory))
            throw new IllegalStateException("Server \"%s\" already exists".formatted(name));

        Mimo.LOGGER.info("Creating server directory at \"%s\"".formatted(instanceDirectory));
        return Files.createDirectories(instanceDirectory);
    }

    public abstract ServerInstance install(String name, MinecraftVersion minecraftVersion, String loaderVersion) throws IOException, InterruptedException;
}
