package com.lalaalal.mimo;

import com.google.gson.JsonParseException;
import com.lalaalal.mimo.content_provider.ContentProvider;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.data.ProjectType;
import com.lalaalal.mimo.exception.MessageComponentException;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.util.HashUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstanceLoader {
    public static final String INSTANCE_DATA_FILE_NAME = "instance.json";
    private static final Pattern JAR_NAME_PATTERN = Pattern.compile("^([a-z]+)-server-([^+]+)\\+(.+)\\.jar$");

    private static final Map<String, ServerInstance> instances = new HashMap<>();

    public static void forget(String serverName) {
        instances.remove(serverName);
    }

    private static ServerInstance createServer(String serverName, String serverFileName, Path directory) throws IOException {
        Matcher matcher = JAR_NAME_PATTERN.matcher(serverFileName);
        if (!matcher.matches()) {
            Mimo.LOGGER.error("\"{}\" is not a valid server jar file name", serverFileName);
            throw new IllegalStateException("Aborted");
        }
        String loaderType = matcher.group(1);
        String loaderVersion = matcher.group(2);
        String minecraftVersion = matcher.group(3);
        Loader loader = new Loader(loaderType, loaderVersion);
        ServerInstance instance = new ServerInstance(serverName, loader, MinecraftVersion.of(minecraftVersion), directory);

        Map<Content, Content.Version> versions = getContentVersions(instance, directory);
        instance.setContents(versions);
        instance.save();

        instances.put(serverName, instance);
        return instance;
    }

    protected static ServerInstance loadServerFromDirectory(Path directory) throws IOException {
        String serverName = directory.getFileName().toString();
        if (instances.containsKey(serverName))
            return instances.get(serverName);

        Mimo.LOGGER.info("Loading instance from directory \"{}\"", directory);
        File[] files = directory.toFile().listFiles((dir, name) -> name.matches(JAR_NAME_PATTERN.pattern()));

        if (files == null || files.length != 1)
            throw new MessageComponentException("Server " + serverName + " not found");
        File jarFile = files[0];
        return createServer(serverName, jarFile.getName(), directory);
    }

    /**
     * Load {@linkplain ServerInstance} from the instance.json file.
     *
     * @param instanceDataFile Path to the instance.json file
     * @return Instance of the server
     * @throws IOException If an I/O error occurs
     */
    protected static ServerInstance loadServerFromFile(File instanceDataFile) throws IOException {
        Path directory = instanceDataFile.getParentFile().toPath();
        String serverName = directory.getFileName().toString();
        if (instances.containsKey(serverName))
            return instances.get(serverName);
        Mimo.LOGGER.info("Loading instance from instance file \"{}\"", instanceDataFile);
        try (BufferedReader reader = new BufferedReader(new FileReader(instanceDataFile))) {
            ServerInstance instance = Mimo.GSON.fromJson(reader, ServerInstance.class);
            if (instance == null)
                return loadServerFromDirectory(directory);
            instances.put(instance.name, instance);
            return instance;
        } catch (JsonParseException exception) {
            Mimo.LOGGER.error("Failed to parse \"{}\" file", instanceDataFile);
            throw new IllegalStateException("Aborted");
        }
    }

    private static Map<Content, Content.Version> getContentVersions(ServerInstance serverInstance, ProjectType projectType, Path directory) throws IOException {
        Path modsPath = directory.resolve(projectType.path);
        File modsDirectory = modsPath.toFile();
        File[] files = modsDirectory.listFiles((dir, name) -> name.endsWith(".jar") || name.endsWith(".zip"));
        if (files == null || files.length == 0)
            return Map.of();
        Map<String, File> hashes = new HashMap<>();
        for (File file : files) {
            String hash = HashUtils.hashFile(file.toPath());
            hashes.put(hash, file);
        }
        Map<Content, Content.Version> versions = new HashMap<>();
        for (ContentProvider contentProvider : Registries.CONTENT_PROVIDERS) {
            Map<Content, Content.Version> current = contentProvider.getLatestVersions(hashes, serverInstance);
            for (Content.Version version : current.values())
                hashes.remove(version.hash());
            versions.putAll(current);
        }
        hashes.forEach((hash, file) -> {
            Content.Version version = Content.Version.custom(hash, file);
            Content content = Content.custom(projectType, serverInstance.loader.type(), version.versionId());
            versions.put(content, version);
        });

        return versions;
    }

    private static Map<Content, Content.Version> getContentVersions(ServerInstance serverInstance, Path directory) throws IOException {
        Map<Content, Content.Version> versions = new HashMap<>();
        versions.putAll(getContentVersions(serverInstance, ProjectType.MOD, directory));
        versions.putAll(getContentVersions(serverInstance, ProjectType.DATAPACK, directory));

        return versions;
    }
}
