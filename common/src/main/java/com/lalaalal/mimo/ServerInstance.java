package com.lalaalal.mimo;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonWriter;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.json.ServerInstanceAdaptor;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.loader.LoaderInstaller;
import com.lalaalal.mimo.modrinth.ModrinthHelper;
import com.lalaalal.mimo.modrinth.Request;
import com.lalaalal.mimo.modrinth.ResponseParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@JsonAdapter(ServerInstanceAdaptor.class)
public class ServerInstance {
    public final String name;
    public final Loader loader;
    public final MinecraftVersion version;
    public final Path path;

    private final Map<Content, ContentInstance> contents = new HashMap<>();
    private final List<ContentInstance> newContents = new ArrayList<>();

    public static ServerInstance from(Path directory) throws IOException {
        File instanceDataFile = directory.resolve(InstanceLoader.INSTANCE_DATA_FILE_NAME).toFile();
        if (instanceDataFile.exists())
            return ServerInstance.load(instanceDataFile);

        ServerInstance serverInstance = InstanceLoader.loadServerFromDirectory(directory);
        Map<String, Content.Version> versions = InstanceLoader.getContentVersions(directory.resolve("mods"));
        List<Content> contents = ModrinthHelper.get(Request.projects(versions.keySet()), ResponseParser.contentListParser(serverInstance));
        for (Content content : contents)
            serverInstance.contents.put(content, new ContentInstance(serverInstance, content, versions.get(content.id())));
        serverInstance.save();
        return serverInstance;
    }

    public static ServerInstance load(File instanceDataFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(instanceDataFilePath))) {
            return Mimo.GSON.fromJson(reader, ServerInstance.class);
        }
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

    public void launch(OutputStream outputStream, InputStream inputStream) throws IOException, InterruptedException {
        String fileName = LoaderInstaller.get(loader.type())
                .getFileName(version, loader.version());
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", fileName, "nogui");
        processBuilder.directory(path.toFile());
        Process process = processBuilder.start();

        Thread thread = new Thread(() -> redirectInputStream(process, inputStream));
        thread.start();

        String line;
        PrintStream writer = new PrintStream(outputStream);
        try (BufferedReader reader = process.inputReader()) {
            while ((line = reader.readLine()) != null)
                writer.println(line);
        }
        thread.interrupt();
        writer.print("Press Enter to continue");
        thread.join();
    }

    private void redirectInputStream(Process process, InputStream inputStream) {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try (BufferedWriter writer = process.outputWriter()) {
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
                writer.flush();
            }
        } catch (IOException ignored) {

        }
    }

    public void setContents(Map<Content, Content.Version> contentVersions) {
        for (Content content : contentVersions.keySet())
            this.contents.put(content, new ContentInstance(this, content, contentVersions.get(content)));
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

    public void removeContent(Content content) throws IOException {
        if (contents.containsKey(content)) {
            ContentInstance contentInstance = contents.get(content);
            contentInstance.removeContent();
            contents.remove(content);
        }
        save();
    }

    public synchronized void updateContents() throws IOException {
        downloadContents();
        for (ContentInstance contentInstance : contents.values()) {
            if (contentInstance.isUpToDate())
                continue;

            contentInstance.downloadContent();
        }
        save();
    }

    public synchronized void downloadContents() throws IOException {
        for (ContentInstance contentInstance : newContents) {
            if (!contentInstance.isVersionSelected())
                contentInstance.selectContentVersion(0);
            contentInstance.downloadContent();
        }
        newContents.clear();
        save();
    }

    public Collection<ContentInstance> getContents() {
        return contents.values();
    }

    public ContentInstance get(Content content) {
        return contents.get(content);
    }

    public void save() throws IOException {
        this.save(path.resolve(InstanceLoader.INSTANCE_DATA_FILE_NAME));
    }

    public void save(Path path) throws IOException {
        try (JsonWriter jsonWriter = new JsonWriter(new FileWriter(path.toFile()))) {
            Mimo.GSON.toJson(this, ServerInstance.class, jsonWriter);
        }
    }
}
