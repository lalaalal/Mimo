package com.lalaalal.mimo.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.util.HttpHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FabricInstaller extends LoaderInstaller {
    public static final String VERSIONS_URL = "https://meta.fabricmc.net/v2/versions/";
    public static final String LAUNCHER_DOWNLOAD_URL = "https://meta.fabricmc.net/v2/versions/loader/%s/%s/%s/server/jar";

    private final List<MinecraftVersion> minecraftVersions;
    private final List<String> loaderVersions;
    private final List<String> installerVersions;

    protected FabricInstaller() throws IOException {
        super(Loader.Type.FABRIC);
        this.minecraftVersions = parseVersions("game").stream()
                .map(MinecraftVersion::of)
                .toList();
        this.loaderVersions = parseVersions("loader");
        this.installerVersions = parseVersions("installer");
    }

    private List<String> parseVersions(String target) throws IOException {
        String data = HttpHelper.sendSimpleHttpRequest(VERSIONS_URL + target);
        JsonArray versions = Mimo.GSON.fromJson(data, JsonArray.class);
        List<String> result = new ArrayList<>();
        for (JsonElement version : versions) {
            String versionName = version.getAsJsonObject().get("version").getAsString();
            result.add(versionName);
        }
        if (result.isEmpty())
            throw new IllegalStateException("Failed to load");

        return List.copyOf(result);
    }

    @Override
    public List<String> getAvailableVersions(MinecraftVersion minecraftVersion) {
        if (minecraftVersions.contains(minecraftVersion))
            return loaderVersions;
        return List.of();
    }

    @Override
    public boolean isValidVersion(MinecraftVersion minecraftVersion, String loaderVersion) {
        return minecraftVersions.contains(minecraftVersion) && loaderVersions.contains(loaderVersion);
    }

    @Override
    protected void install(Path instanceDirectory, MinecraftVersion minecraftVersion, String loaderVersion) throws IOException {
        String fabricInstallerVersion = this.installerVersions.getFirst();
        String url = LAUNCHER_DOWNLOAD_URL.formatted(minecraftVersion, loaderVersion, fabricInstallerVersion);
        String fileName = getFileName(minecraftVersion, loaderVersion);
        Path file = instanceDirectory.resolve(fileName);
        Mimo.LOGGER.info("Downloading server jar file at \"%s\"".formatted(file));
        HttpHelper.download(url, file);
    }
}
