package com.lalaalal.mimo;

import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.modrinth.ModrinthHelper;
import com.lalaalal.mimo.modrinth.Request;
import com.lalaalal.mimo.modrinth.ResponseParser;
import com.lalaalal.mimo.util.HashUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstanceLoader {
    public static final String INSTANCE_DATA_FILE_NAME = "instance.json";
    private static final Pattern JAR_NAME_PATTERN = Pattern.compile("^([a-z]+)-server-([^+]+)\\+(.+)\\.jar$");

    private static ServerInstance createServer(String serverName, String serverFileName, Path directory) throws IOException {
        Matcher matcher = JAR_NAME_PATTERN.matcher(serverFileName);
        if (!matcher.matches())
            throw new IllegalStateException("\"%s\" is not a valid server jar file name".formatted(serverFileName));
        String loaderType = matcher.group(1);
        String loaderVersion = matcher.group(2);
        String minecraftVersion = matcher.group(3);
        Loader loader = new Loader(loaderType, loaderVersion);
        return new ServerInstance(serverName, loader, MinecraftVersion.of(minecraftVersion), directory);
    }

    protected static ServerInstance loadServerFromDirectory(Path directory) throws IOException {
        String serverName = directory.getFileName().toString();
        File[] files = directory.toFile().listFiles((dir, name) -> name.matches(JAR_NAME_PATTERN.pattern()));

        if (files == null || files.length != 1)
            throw new IllegalStateException("Server " + serverName + " not found");
        File jarFile = files[0];
        return createServer(serverName, jarFile.getName(), directory);
    }

    /**
     * Load {@linkplain ServerInstance} from the instance.json file.
     *
     * @param instanceDataFilePath Path to the instance.json file
     * @return Instance of the server
     * @throws IOException If an I/O error occurs
     */
    protected static ServerInstance loadServerFromFile(File instanceDataFilePath) throws IOException {
        Mimo.LOGGER.info("Loading instance from instance file \"%s\"".formatted(instanceDataFilePath));
        try (BufferedReader reader = new BufferedReader(new FileReader(instanceDataFilePath))) {
            return Mimo.GSON.fromJson(reader, ServerInstance.class);
        }
    }

    protected static Map<String, Content.Version> getContentVersions(Path modsPath) throws IOException {
        File modsDirectory = modsPath.toFile();
        File[] modFiles = modsDirectory.listFiles((dir, name) -> name.endsWith(".jar") || name.endsWith(".zip"));
        if (modFiles == null)
            return Map.of();
        String[] hashes = new String[modFiles.length];
        for (int index = 0; index < modFiles.length; index++)
            hashes[index] = HashUtils.hashFile(modFiles[index].toPath());
        return ModrinthHelper.get(Request.versions(hashes), ResponseParser::parseVersionListWithProjectId);
    }
}
