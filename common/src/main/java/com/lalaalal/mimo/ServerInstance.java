package com.lalaalal.mimo;

import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.modrinth.ModrinthHelper;
import com.lalaalal.mimo.modrinth.Request;
import com.lalaalal.mimo.modrinth.ResponseParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ServerInstance {
    public final String name;
    public final Loader loader;
    public final MinecraftVersion version;
    public final Path path;
    private final Map<Content, ContentInstance> contents = new HashMap<>();
    private final List<ContentInstance> newContents = new ArrayList<>();

    public static ServerInstance from(Path directory) throws IOException {
        ServerInstance serverInstance = InstanceLoader.loadServerFromDirectory(directory);
        Map<String, Content.Version> versions = InstanceLoader.getContentVersions(directory.resolve("mods"));
        List<Content> contents = ModrinthHelper.get(Request.projects(versions.keySet()), ResponseParser::parseContentList);
        for (Content content : contents)
            serverInstance.contents.put(content, new ContentInstance(serverInstance, content, versions.get(content.id())));
        return serverInstance;
    }

    public ServerInstance(String name, Loader loader, MinecraftVersion version, Path path) throws IOException {
        this.name = name;
        this.loader = loader;
        this.version = version;
        this.path = path;
        Files.createDirectories(path);
    }

    public ServerInstance(String name, Loader loader, MinecraftVersion version) throws IOException {
        this(name, loader, version, Mimo.getInstanceContainerDirectory().resolve(name));
    }

    public void addContent(Content content) {
        if (this.contains(content))
            return;
        ContentInstance contentInstance = new ContentInstance(this, content);
        contents.put(content, contentInstance);
        newContents.add(contentInstance);
    }

    public boolean contains(Content content) {
        return contents.containsKey(content);
    }

    public void removeContent(Content content) {
        contents.remove(content);
    }

    public synchronized void updateContents() throws IOException {
        for (ContentInstance contentInstance : contents.values()) {
            if (contentInstance.isUpToDate())
                continue;

            contentInstance.downloadContent();
        }
    }

    public synchronized void downloadContents() throws IOException {
        for (ContentInstance contentInstance : newContents) {
            if (!contentInstance.isVersionSelected())
                contentInstance.selectContentVersion(0);
            contentInstance.downloadContent();
        }
        newContents.clear();
    }

    public Collection<ContentInstance> getContents() {
        return contents.values();
    }

    public ContentInstance get(Content content) {
        return contents.get(content);
    }
}
