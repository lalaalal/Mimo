package com.lalaalal.mimo;

import com.google.gson.FormattingStyle;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonWriter;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.data.ProjectType;
import com.lalaalal.mimo.json.ServerInstanceAdaptor;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.loader.LoaderInstaller;
import com.lalaalal.mimo.modrinth.ModrinthHelper;
import com.lalaalal.mimo.modrinth.Request;
import com.lalaalal.mimo.modrinth.ResponseParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Instance of the server.
 * Contains information about the server and its contents.
 * Provides methods for managing the server.
 *
 * @see #from(Path)
 * @see #addContent(Content)
 * @see #removeContent(Content)
 * @see #updateContents()
 */
@JsonAdapter(ServerInstanceAdaptor.class)
public class ServerInstance {
    public final String name;
    public final Loader loader;
    public final MinecraftVersion version;
    public final Path path;

    private final Map<Content, ContentInstance> contents = new HashMap<>();

    /**
     * Load {@linkplain ServerInstance} from directory.
     * If the instance.json file exists in the directory, it will be loaded.
     *
     * @param directory Directory of the instance
     * @return Instance of the server
     * @throws IOException If an I/O error occurs
     */
    public static ServerInstance from(Path directory) throws IOException {
        File instanceDataFile = directory.resolve(InstanceLoader.INSTANCE_DATA_FILE_NAME).toFile();
        if (instanceDataFile.exists())
            return InstanceLoader.loadServerFromFile(instanceDataFile);

        Mimo.LOGGER.info("Loading instance from directory \"%s\"".formatted(directory));
        ServerInstance serverInstance = InstanceLoader.loadServerFromDirectory(directory);
        Map<Content, Content.Version> versions = getContentVersions(serverInstance, directory);
        serverInstance.setContents(versions);
        serverInstance.save();
        return serverInstance;
    }

    private static Map<Content, Content.Version> getContentVersions(ServerInstance serverInstance, Path directory) throws IOException {
        Map<String, Content.Version> versions = new HashMap<>();
        versions.putAll(InstanceLoader.getContentVersions(directory.resolve(ProjectType.MOD.path)));
        versions.putAll(InstanceLoader.getContentVersions(directory.resolve(ProjectType.DATAPACK.path)));
        List<Content> contents = ModrinthHelper.get(
                Request.projects(versions.keySet()),
                ResponseParser.contentListParser(serverInstance)
        );
        Map<Content, Content.Version> result = new HashMap<>();
        contents.forEach(content -> result.put(content, versions.get(content.id())));
        return result;
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

    public void checkUpdate() {
        int outOfDateContents = 0;
        int notDownloadedContents = 0;
        for (ContentInstance contentInstance : contents.values()) {
            if (!contentInstance.isDownloaded())
                notDownloadedContents += 1;
            if (!contentInstance.isUpToDate())
                outOfDateContents += 1;
        }
        if (outOfDateContents > 0)
            Mimo.LOGGER.warning("There are %d out dated content(s)".formatted(outOfDateContents));
        if (notDownloadedContents > 0)
            Mimo.LOGGER.warning("There are %d content(s) not downloaded".formatted(notDownloadedContents));
    }

    public void launch(OutputStream outputStream, InputStream inputStream) throws IOException {
        Mimo.LOGGER.info("Launching server \"%s\"".formatted(name));
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
        for (Content content : contentVersions.keySet()) {
            Mimo.LOGGER.info("Adding \"%s\" to \"%s\" instance".formatted(content.slug(), name));
            this.contents.put(content, new ContentInstance(this, content, contentVersions.get(content)));
        }
        checkUpdate();
    }

    /**
     * Add content to the instance.
     *
     * @param content Content to add
     * @see ModrinthHelper#get(Request, Function)
     */
    public void addContent(Content content) {
        if (this.contains(content))
            return;
        Mimo.LOGGER.info("Adding content \"%s\"".formatted(content.slug()));
        ContentInstance contentInstance = new ContentInstance(this, content);
        contents.put(content, contentInstance);
    }

    public boolean contains(Content content) {
        return contents.containsKey(content);
    }

    /**
     * Remove content from the instance.
     * Save the instance after this update.
     *
     * @param content Content to remove
     * @throws IOException If an I/O error occurs
     */
    public void removeContent(Content content) throws IOException {
        if (contents.containsKey(content)) {
            ContentInstance contentInstance = contents.get(content);
            contentInstance.removeContent();
            contents.remove(content);
        }
        save();
    }

    /**
     * {@link #downloadContents()} and update contents.
     * Save the instance after this update.
     *
     * @throws IOException If an I/O error occurs
     */
    public synchronized void updateContents() throws IOException {
        downloadContents();
        Mimo.LOGGER.info("Updating contents for \"%s\"".formatted(name));
        for (ContentInstance contentInstance : contents.values()) {
            if (contentInstance.isUpToDate())
                continue;

            contentInstance.downloadContent();
        }
        save();
    }

    public synchronized void downloadContents() throws IOException {
        Mimo.LOGGER.info("Downloading contents for \"%s\"".formatted(name));
        for (ContentInstance contentInstance : contents.values()) {
            if (contentInstance.isDownloaded())
                continue;
            if (!contentInstance.isVersionSelected())
                contentInstance.selectContentVersion(0);
            contentInstance.downloadContent();
        }
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
        Mimo.LOGGER.debug("Saving instance \"%s\"".formatted(name));
        try (JsonWriter jsonWriter = new JsonWriter(new FileWriter(path.toFile()))) {
            jsonWriter.setFormattingStyle(FormattingStyle.PRETTY);
            Mimo.GSON.toJson(this, ServerInstance.class, jsonWriter);
        }
    }
}
