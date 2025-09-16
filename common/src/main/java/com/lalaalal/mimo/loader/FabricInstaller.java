package com.lalaalal.mimo.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.util.HttpHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FabricInstaller extends LoaderInstaller {
    public static final String MINECRAFT_VERSIONS_URL = "https://meta.fabricmc.net/v2/versions/";
    public static final String FABRIC_VERSIONS_URL = "https://meta.fabricmc.net/v2/versions/loader";
    public static final String LAUNCHER_DOWNLOAD_URL = "https://meta.fabricmc.net/v2/versions/loader/%s/%s/%s/server/jar";
    public static final String FABRIC_INSTALLER_VERSION = "1.0.1";

    private final List<MinecraftVersion> minecraftVersions;
    private final List<String> fabricVersions;

    protected FabricInstaller() throws IOException {
        super(Loader.Type.FABRIC);
        Mimo.LOGGER.info("Preparing fabric installer");
        this.minecraftVersions = loadMinecraftVersions();
        this.fabricVersions = loadFabricVersions();
    }

    private List<String> loadFabricVersions() throws IOException {
        String data = HttpHelper.sendSimpleHttpRequest(FABRIC_VERSIONS_URL);
        JsonArray fabricVersions = Mimo.GSON.fromJson(data, JsonArray.class);
        List<String> versions = new ArrayList<>();
        for (JsonElement version : fabricVersions) {
            String versionName = version.getAsJsonObject().get("version").getAsString();
            versions.add(versionName);
        }

        return versions;
    }

    private List<MinecraftVersion> loadMinecraftVersions() throws IOException {
        String data = HttpHelper.sendSimpleHttpRequest(MINECRAFT_VERSIONS_URL);
        JsonObject jsonObject = Mimo.GSON.fromJson(data, JsonObject.class);
        JsonArray minecraftVersions = jsonObject.get("game").getAsJsonArray();
        List<MinecraftVersion> versions = new ArrayList<>();
        for (JsonElement minecraftVersion : minecraftVersions) {
            String versionName = minecraftVersion.getAsJsonObject().get("version").getAsString();
            versions.add(MinecraftVersion.of(versionName));
        }
        return versions;
    }

    @Override
    public List<String> getAvailableVersions(MinecraftVersion minecraftVersion) {
        if (minecraftVersions.contains(minecraftVersion))
            return fabricVersions;
        return List.of();
    }

    @Override
    public boolean isValidVersion(MinecraftVersion minecraftVersion, String loaderVersion) {
        return minecraftVersions.contains(minecraftVersion) && fabricVersions.contains(loaderVersion);
    }

    @Override
    public ServerInstance install(String name, MinecraftVersion minecraftVersion, String loaderVersion) throws IOException {
        Mimo.LOGGER.info("Installing fabric server [%s] (%s)".formatted(minecraftVersion, name));
        if (!isValidVersion(minecraftVersion, loaderVersion))
            throw new IllegalArgumentException("Given version is not valid (%s, %s)".formatted(minecraftVersion, loaderVersion));

        Path instanceDirectory = createInstanceDirectory(name);

        String url = LAUNCHER_DOWNLOAD_URL.formatted(minecraftVersion, loaderVersion, FABRIC_INSTALLER_VERSION);
        String fileName = getFileName(minecraftVersion, loaderVersion);
        Path file = instanceDirectory.resolve(fileName);
        Mimo.LOGGER.info("Downloading server jar file at \"%s\"".formatted(file));
        HttpHelper.download(url, file);

        Path eula = instanceDirectory.resolve("eula.txt");
        Mimo.LOGGER.info("Creating eula.txt file at \"%s\"".formatted(eula));
        Files.writeString(eula, "eula=true\n");

        return new ServerInstance(name, new Loader(loaderType, loaderVersion), minecraftVersion);
    }
}
