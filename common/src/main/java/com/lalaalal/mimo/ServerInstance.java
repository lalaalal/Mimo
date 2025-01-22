package com.lalaalal.mimo;

import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ContentInstance;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.loader.Loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ServerInstance {
    public final String name;
    public final Loader loader;
    public final MinecraftVersion version;
    public final Path path;
    private final List<ContentInstance> contents = new ArrayList<>();
    private final List<ContentInstance> newContents = new ArrayList<>();

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
        ContentInstance contentInstance = new ContentInstance(this, content);
        contents.add(contentInstance);
        newContents.add(contentInstance);
    }

    public void removeContent(int index) {
        contents.remove(index);
    }

    public void downloadContents() throws IOException {
        for (ContentInstance contentInstance : newContents) {
            if (!contentInstance.isVersionSelected())
                contentInstance.selectContentVersion(0);
            contentInstance.downloadContent();
        }
        newContents.clear();
    }

    public List<ContentInstance> getContents() {
        return contents;
    }
}
