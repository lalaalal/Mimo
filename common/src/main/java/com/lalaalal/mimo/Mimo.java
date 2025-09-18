package com.lalaalal.mimo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.json.MimoExcludeStrategy;
import com.lalaalal.mimo.json.ServerInstanceAdaptor;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.loader.LoaderInstaller;
import com.lalaalal.mimo.logging.Logger;
import com.lalaalal.mimo.modrinth.ModrinthHelper;
import com.lalaalal.mimo.modrinth.Request;
import com.lalaalal.mimo.modrinth.ResponseParser;
import com.lalaalal.mimo.util.DirectoryRemover;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Mimo {
    private static ServerInstance currentServerInstance = null;

    public static final Gson GSON = new GsonBuilder()
            .addSerializationExclusionStrategy(new MimoExcludeStrategy())
            .addDeserializationExclusionStrategy(new MimoExcludeStrategy())
            .registerTypeAdapter(ServerInstanceAdaptor.class, new ServerInstanceAdaptor())
            .create();

    public static final Logger LOGGER = Logger.stdout();

    public static void initialize() throws IOException {
        Files.createDirectories(getInstanceContainerDirectory());
        LoaderInstaller.initialize();
    }

    public static Path getInstanceContainerDirectory() {
        return Platform.get().defaultMimoDirectory.resolve("servers");
    }

    public static ServerInstance load(String name) throws IOException {
        return currentServerInstance = ServerInstance.from(getInstanceContainerDirectory().resolve(name));
    }

    public static void install(Loader.Type type, String name, MinecraftVersion minecraftVersion, String loaderVersion) throws IOException, InterruptedException {
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

    public static void add(String slug) throws IOException {
        add(slug, false);
    }

    public static void add(String slug, boolean immediateUpdate) throws IOException {
        ServerInstance serverInstance = currentInstanceOrThrow();
        Content content = ModrinthHelper.get(Request.project(slug), ResponseParser.contentParser(serverInstance));
        serverInstance.addContent(content);
        if (immediateUpdate)
            serverInstance.downloadContents();
        serverInstance.checkUpdate();
    }

    public static void add(List<String> slugs) throws IOException {
        ServerInstance serverInstance = currentInstanceOrThrow();
        List<Content> contents = ModrinthHelper.get(Request.projects(slugs), ResponseParser.contentListParser(serverInstance));
        contents.forEach(serverInstance::addContent);
        serverInstance.checkUpdate();
    }

    public static void removeContent(String slug) throws IOException {
        ServerInstance serverInstance = currentInstanceOrThrow();
        Content content = serverInstance.getContents().stream()
                .map(ContentInstance::content)
                .filter(_content -> _content.slug().equals(slug) || _content.id().equals(slug))
                .findAny()
                .orElseGet(() -> ModrinthHelper.get(Request.project(slug), ResponseParser.contentParser(serverInstance)));
        serverInstance.removeContent(content);
    }

    public static void removeServer(String serverName) throws IOException {
        InstanceLoader.forget(serverName);
        Path instanceDirectory = getInstanceContainerDirectory().resolve(serverName);
        if (Files.exists(instanceDirectory)) {
            Mimo.LOGGER.info("Deleting server \"%s\"".formatted(instanceDirectory));
            Files.walkFileTree(instanceDirectory, new DirectoryRemover());
        } else {
            Mimo.LOGGER.warning("No such server at \"%s\"".formatted(instanceDirectory));
        }
    }

    public static void update() throws IOException {
        ServerInstance serverInstance = currentInstanceOrThrow();
        serverInstance.updateContents();
    }
}
