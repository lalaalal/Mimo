package com.lalaalal.mimo.loader;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.registry.RegistryItem;
import com.lalaalal.mimo.util.HttpHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ForgeLikeInstaller extends LoaderInstaller {
    public static final Pattern VERSION_PATTERN = Pattern.compile("^([0-9]+(\\.[0-9]+)+)-.*$");
    public static final String INSTALLER_FILE_NAME = "installer.jar";
    public static final String LAUNCHER_FILE_NAME = "run.sh";

    protected final Map<MinecraftVersion, List<String>> versionMapping = new HashMap<>();
    private final String installerDownloadUrl;
    private final String installServerArgument;

    protected ForgeLikeInstaller(Loader.Type type, String installerDownloadUrl, String installServerArgument) throws IOException {
        super(type);
        this.installerDownloadUrl = installerDownloadUrl;
        this.installServerArgument = installServerArgument;
        loadVersions();
    }

    protected abstract void loadVersions() throws IOException;

    protected List<String> getMinecraftVersionSpecificLoaderVersions(MinecraftVersion minecraftVersion) {
        return versionMapping.computeIfAbsent(minecraftVersion, key -> new ArrayList<>());
    }

    protected Optional<MinecraftVersion> parseMinecraftVersion(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (matcher.matches())
            return Optional.of(MinecraftVersion.of(matcher.group(1)));
        return Optional.empty();
    }

    @Override
    public String getLauncherFileName(MinecraftVersion minecraftVersion, String loaderVersion) {
        return LAUNCHER_FILE_NAME;
    }

    @Override
    public List<String> getAvailableVersions(MinecraftVersion minecraftVersion) {
        return versionMapping.getOrDefault(minecraftVersion, List.of());
    }

    @Override
    public boolean isValidVersion(MinecraftVersion minecraftVersion, String loaderVersion) {
        if (versionMapping.containsKey(minecraftVersion))
            return versionMapping.get(minecraftVersion).contains(loaderVersion);
        return false;
    }

    @Override
    protected void processInstall(Path instanceDirectory, MinecraftVersion minecraftVersion, String loaderVersion) throws IOException, InterruptedException {
        String url = installerDownloadUrl.formatted(loaderVersion);
        Path installer = instanceDirectory.resolve(INSTALLER_FILE_NAME);
        if (Files.exists(installer)) {
            Mimo.LOGGER.warning("installer.jar already exists");
            Mimo.LOGGER.warning("Skip downloading installer.jar");
        } else {
            Mimo.LOGGER.info("Downloading {} installer.jar file at \"{}\"", loaderType, installer);
            HttpHelper.download(url, installer);
        }

        if (!Files.exists(installer)) {
            Mimo.LOGGER.error("Failed to download {} installer!!", loaderVersion);
            throw new IllegalStateException("Stopped");
        }

        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", INSTALLER_FILE_NAME, installServerArgument);
        processBuilder.directory(instanceDirectory.toFile());
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        Mimo.LOGGER.info("Start installing {} server...", loaderVersion);
        Process process = processBuilder.start();
        process.waitFor();
    }

    @Override
    protected RegistryItem<ServerLauncher> getServerLauncher() {
        return ServerLauncher.FORGE_LIKE;
    }
}
