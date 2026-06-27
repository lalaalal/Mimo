package com.lalaalal.mimo.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.util.HttpHelper;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeoForgeInstaller extends ForgeLikeInstaller {
    public static final String VERSIONS_URL = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge";
    public static final String INSTALLER_DOWNLOAD_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/%1$s/neoforge-%1$s-installer.jar";
    public static final Pattern VERSION_PATTERN = Pattern.compile("^([0-9]+)((\\.[0-9]+)+).*$");

    protected NeoForgeInstaller() throws IOException {
        super(Loader.Type.NEOFORGE, INSTALLER_DOWNLOAD_URL, "--install-server");
    }

    @Override
    protected void loadVersions() throws IOException {
        Mimo.LOGGER.info("Loading NeoForge versions");
        String data = HttpHelper.sendSimpleHttpRequest(VERSIONS_URL);
        JsonObject object = Mimo.GSON.fromJson(data, JsonObject.class);
        JsonArray versions = object.get("versions").getAsJsonArray();
        for (JsonElement element : versions) {
            String version = element.getAsString();

            parseMinecraftVersion(version).ifPresent(minecraftVersion -> {
                getMinecraftVersionSpecificLoaderVersions(minecraftVersion).addFirst(version);
            });
        }
    }

    @Override
    protected Optional<MinecraftVersion> parseMinecraftVersion(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (matcher.matches()) {
            String first = matcher.group(1);
            String remains = matcher.group(2).substring(1);
            String[] subVersions = remains.split("\\.");
            if (subVersions.length == 2)
                return Optional.of(MinecraftVersion.legacy(Integer.parseInt(first), Integer.parseInt(subVersions[0])));
            if (subVersions.length == 3)
                return Optional.of(MinecraftVersion.stable(first, subVersions[0], subVersions[1]));
        }
        return Optional.empty();
    }
}
