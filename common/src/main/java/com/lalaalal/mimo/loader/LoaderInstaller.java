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

/**
 * Installs a server.
 *
 * @see FabricInstaller
 * @see NeoForgeInstaller
 * @see LoaderInstaller#get(Loader.Type)
 */
public abstract class LoaderInstaller {
    private static final String FILE_NAME_FORMAT = "%s-server-%s+%s.jar";
    private static final Map<Loader.Type, LoaderInstaller> INSTALLERS = new HashMap<>();
    protected static final Path INSTANCES_PATH = Mimo.getInstanceContainerDirectory();

    public final Loader.Type loaderType;

    public static void initialize() throws IOException {
        INSTALLERS.put(Loader.Type.FABRIC, new FabricInstaller());
        INSTALLERS.put(Loader.Type.NEOFORGE, new NeoForgeInstaller());
    }

    public static LoaderInstaller get(Loader.Type loader) {
        return INSTALLERS.get(loader);
    }

    protected LoaderInstaller(Loader.Type loaderType) {
        Mimo.LOGGER.info("Preparing " + loaderType + " installer");
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
            Mimo.LOGGER.warning("Server \"{}\" already exists", name);

        Mimo.LOGGER.info("Creating server directory at \"{}\"", instanceDirectory);
        return Files.createDirectories(instanceDirectory);
    }

    protected void createEulaFile(Path instanceDirectory) throws IOException {
        Path eula = instanceDirectory.resolve("eula.txt");
        Mimo.LOGGER.info("Creating eula.txt file at \"{}\"", eula);
        Files.writeString(eula, "eula=true\n");
    }

    public ServerInstance install(String name, MinecraftVersion minecraftVersion, String loaderVersion) throws IOException, InterruptedException {
        Mimo.LOGGER.info("Installing {} server [{}] ({})", loaderType, minecraftVersion, name);
        if (minecraftVersion.type() != MinecraftVersion.Type.STABLE)
            Mimo.LOGGER.warning("Selected minecraft version [{}] is not a stable version", minecraftVersion);
        if (!isValidVersion(minecraftVersion, loaderVersion))
            throw new IllegalArgumentException("Given version is not valid (%s, %s)".formatted(minecraftVersion, loaderVersion));

        Path instanceDirectory = createInstanceDirectory(name);
        processInstall(instanceDirectory, minecraftVersion, loaderVersion);

        createEulaFile(instanceDirectory);
        Mimo.LOGGER.info("Installed server \"{}\" ({} {}) [{}]", name, loaderType, loaderVersion, minecraftVersion);
        return new ServerInstance(name, new Loader(loaderType, loaderVersion), minecraftVersion);
    }

    protected abstract void processInstall(Path instanceDirectory, MinecraftVersion minecraftVersion, String loaderVersion) throws IOException, InterruptedException;
}
