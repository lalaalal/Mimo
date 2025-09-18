package com.lalaalal.mimo.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.util.HttpHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeoForgeInstaller extends LoaderInstaller {
    public static final String NEOFORGE_VERSIONS_URL = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge";
    public static final Pattern NEOFORGE_VERSION_PATTERN = Pattern.compile("^([0-9]+)\\.([0-9]+)\\..*$");

    public static final String INSTALLER_DOWNLOAD_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/%1$s/neoforge-%1$s-installer.jar";
    public static final String INSTALLER_FILE_NAME = "installer.jar";

    private final Map<MinecraftVersion, List<String>> versionMapping = new HashMap<>();

    protected NeoForgeInstaller() throws IOException {
        super(Loader.Type.NEOFORGE);
        loadNeoForgeVersions();
    }

    private void loadNeoForgeVersions() throws IOException {
        String data = HttpHelper.sendSimpleHttpRequest(NEOFORGE_VERSIONS_URL);
        JsonObject object = Mimo.GSON.fromJson(data, JsonObject.class);
        JsonArray versions = object.get("versions").getAsJsonArray();
        for (JsonElement element : versions) {
            String version = element.getAsString();

            Matcher matcher = NEOFORGE_VERSION_PATTERN.matcher(version);
            if (matcher.matches()) {
                int majorVersion = Integer.parseInt(matcher.group(1));
                int minorVersion = Integer.parseInt(matcher.group(2));
                MinecraftVersion minecraftVersion = MinecraftVersion.of(majorVersion, minorVersion);
                List<String> minecraftVersionSpecificLoaderVersions = versionMapping.computeIfAbsent(minecraftVersion, key -> new ArrayList<>());
                minecraftVersionSpecificLoaderVersions.addFirst(version);
            }
        }
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
        String url = INSTALLER_DOWNLOAD_URL.formatted(loaderVersion);
        Path installer = instanceDirectory.resolve(INSTALLER_FILE_NAME);
        if (Files.exists(installer)) {
            Mimo.LOGGER.warning("installer.jar already exists");
            Mimo.LOGGER.warning("Skip downloading installer.jar");
        } else {
            Mimo.LOGGER.info("Downloading NeoForge installer.jar file at \"%s\"".formatted(installer));
            HttpHelper.download(url, installer);
        }

        if (!Files.exists(installer)) {
            Mimo.LOGGER.error("Failed to download neoforge installer!!");
            throw new IllegalStateException("Stopped");
        }

        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", INSTALLER_FILE_NAME, "--install-server", "--server-starter");
        processBuilder.directory(instanceDirectory.toFile());

        Mimo.LOGGER.info("Start installing neoforge server...");
        Process process = processBuilder.start();
        process.waitFor();

        Path originalFile = instanceDirectory.resolve("server.jar");
        String fileName = getFileName(minecraftVersion, loaderVersion);
        Path file = instanceDirectory.resolve(fileName);
        if (!originalFile.toFile().renameTo(file.toFile()))
            throw new IllegalStateException("Failed to rename \"%s\" to \"%s\"".formatted(originalFile, file));
    }
}
