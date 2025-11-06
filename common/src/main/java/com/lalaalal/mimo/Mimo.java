package com.lalaalal.mimo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lalaalal.mimo.content_provider.ContentProvider;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.exception.MessageComponentException;
import com.lalaalal.mimo.json.ContentProviderAdaptor;
import com.lalaalal.mimo.json.MimoExcludeStrategy;
import com.lalaalal.mimo.json.ServerInstanceAdaptor;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.loader.LoaderInstaller;
import com.lalaalal.mimo.logging.Logger;
import com.lalaalal.mimo.util.DirectoryRemover;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class Mimo {
    private static ServerInstance currentServerInstance = null;

    public static final Gson GSON = new GsonBuilder()
            .addSerializationExclusionStrategy(new MimoExcludeStrategy())
            .addDeserializationExclusionStrategy(new MimoExcludeStrategy())
            .registerTypeAdapter(ServerInstanceAdaptor.class, new ServerInstanceAdaptor())
            .registerTypeAdapter(ContentProvider.class, new ContentProviderAdaptor())
            .create();

    public static final Logger LOGGER = Logger.stdout();

    public static void initialize() throws IOException {
        Files.createDirectories(getInstanceContainerDirectory());
        LoaderInstaller.initialize();
        ContentProvider.initialize();
    }

    public static Path getInstanceContainerDirectory() {
        return Platform.get().defaultMimoDirectory.resolve("servers");
    }

    public static ServerInstance load(String name) throws IOException {
        return currentServerInstance = ServerInstance.from(getInstanceContainerDirectory().resolve(name));
    }

    public static ServerInstance rescan(String name) throws IOException {
        InstanceLoader.forget(name);
        Path directory = getInstanceContainerDirectory().resolve(name);
        Path instanceFile = directory.resolve(InstanceLoader.INSTANCE_DATA_FILE_NAME);
        Files.deleteIfExists(instanceFile);
        return currentServerInstance = ServerInstance.from(directory);
    }

    public static void install(Loader.Type type, String name, MinecraftVersion minecraftVersion, String loaderVersion) throws IOException, InterruptedException {
        currentServerInstance = LoaderInstaller.get(type).install(name, minecraftVersion, loaderVersion);
    }

    public static void install(Loader.Type type, String name, MinecraftVersion minecraftVersion) throws IOException, InterruptedException {
        LoaderInstaller installer = LoaderInstaller.get(type);
        List<String> versions = installer.getAvailableVersions(minecraftVersion);
        if (versions.isEmpty()) {
            Mimo.LOGGER.error("There's no available {} version for minecraft [{}]", type, minecraftVersion);
            throw new IllegalStateException("Aborted");
        }
        String loaderVersion = versions.getFirst();
        currentServerInstance = LoaderInstaller.get(type).install(name, minecraftVersion, loaderVersion);
    }

    public static String[] getServers() {
        File directory = getInstanceContainerDirectory().toFile();
        String[] result = directory.list();
        if (result == null)
            return new String[0];
        return result;
    }

    public static ServerInstance currentInstanceOrThrow() {
        if (currentServerInstance == null)
            throw new IllegalStateException("No instance loaded");
        return currentServerInstance;
    }

    public static Optional<ServerInstance> currentInstance() {
        return Optional.ofNullable(currentServerInstance);
    }

    public static void add(String slug, ContentProvider provider) throws IOException {
        add(slug, provider, false);
    }

    public static void add(String slug, ContentProvider provider, boolean immediateUpdate) throws IOException {
        ServerInstance serverInstance = currentInstanceOrThrow();
        Content content = provider.getContentWithSlug(slug, serverInstance);
        serverInstance.addContent(content);
        if (immediateUpdate)
            serverInstance.downloadContents();
        serverInstance.checkUpdate();
        save();
    }

    public static void removeContent(String slug) throws IOException {
        ServerInstance serverInstance = currentInstanceOrThrow();
        serverInstance.removeContent(slug);
    }

    public static void removeServer(String serverName) throws IOException {
        InstanceLoader.forget(serverName);
        Path instanceDirectory = getInstanceContainerDirectory().resolve(serverName);
        if (Files.exists(instanceDirectory)) {
            Mimo.LOGGER.info("Deleting server \"{}\"", instanceDirectory);
            Files.walkFileTree(instanceDirectory, new DirectoryRemover());
        } else {
            Mimo.LOGGER.warning("No such server at \"{}\"", instanceDirectory);
        }
        currentServerInstance = null;
    }

    public static void excludeUpdate(List<String> slugs) {
        ServerInstance serverInstance = currentInstanceOrThrow();
        serverInstance.excludeUpdate(slugs);
    }

    public static void update() throws IOException {
        ServerInstance serverInstance = currentInstanceOrThrow();
        serverInstance.updateContents();
    }

    public static void save() throws IOException {
        ServerInstance serverInstance = currentInstanceOrThrow();
        serverInstance.save();
    }

    public static void changeContentVersion(String slug, int index) {
        ServerInstance serverInstance = currentInstanceOrThrow();
        if (!serverInstance.contains(slug))
            throw new MessageComponentException("[%s] No content found for %s".formatted(serverInstance, slug));
        ContentInstance contentInstance = serverInstance.get(slug);
        contentInstance.selectContentVersion(index);
        LOGGER.info("Execute \"download\" to download selected version");
        LOGGER.info("Execute \"update exclude {}\" to prevent auto update", slug);
    }

    public static void download() throws IOException {
        ServerInstance serverInstance = currentInstanceOrThrow();
        serverInstance.downloadContents();
    }

    public static void checkUpdate() throws IOException {
        ServerInstance serverInstance = currentInstanceOrThrow();
        serverInstance.checkUpdate();
        save();
    }

    public static void includeUpdate(List<String> slugs) throws IOException {
        ServerInstance serverInstance = currentInstanceOrThrow();
        serverInstance.includeUpdate(slugs);
        save();
    }
}
