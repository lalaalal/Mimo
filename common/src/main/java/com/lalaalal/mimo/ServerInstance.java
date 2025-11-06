package com.lalaalal.mimo;

import com.google.gson.FormattingStyle;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonWriter;
import com.lalaalal.mimo.content_provider.RequestCollector;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.json.ServerInstanceAdaptor;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.loader.LoaderInstaller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    private final RequestCollector collector = new RequestCollector();

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
        return InstanceLoader.loadServerFromDirectory(directory);
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
            Mimo.LOGGER.warning("There are {} out dated content(s)", outOfDateContents);
        if (notDownloadedContents > 0)
            Mimo.LOGGER.warning("There are {} content(s) not downloaded", notDownloadedContents);
    }

    public void launch() throws IOException, InterruptedException {
        Mimo.LOGGER.info("Launching server \"{}\"", name);
        String fileName = LoaderInstaller.get(loader.type())
                .getFileName(version, loader.version());
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", fileName, "nogui");
        processBuilder.directory(path.toFile());
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        Process process = processBuilder.start();
        process.waitFor();
    }

    public void setContents(Map<Content, Content.Version> contentVersions) {
        for (Content content : contentVersions.keySet()) {
            Mimo.LOGGER.info("Loading \"{}\" to \"{}\" instance", content.slug(), name);
            ContentInstance contentInstance = new ContentInstance(this, content, contentVersions.get(content));
            contents.put(content, contentInstance);
            collector.add(contentInstance);
        }

        RequestCollector.Distributor<Content.Version> distributor = collector.submit(
                RequestCollector.Type.MULTIPLE_LATEST_VERSION,
                RequestCollector.multipleLatestVersion(this)
        );
        contents.values().forEach(contentInstance -> contentInstance.setLatestVersion(distributor));
    }

    /**
     * Add content to the instance.
     *
     * @param content Content to add
     */
    public void addContent(Content content) {
        if (this.contains(content))
            return;
        Mimo.LOGGER.info("[{}] Adding content \"{}\"", this, content.slug());
        ContentInstance contentInstance = new ContentInstance(this, content);
        contents.put(content, contentInstance);
        collector.add(contentInstance);
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
            Mimo.LOGGER.info("[{}] Removing content \"{}\"", this, content.slug());
            ContentInstance contentInstance = contents.get(content);
            contentInstance.removeContent();
            contents.remove(content);
            collector.remove(contentInstance);
            save();
        }
    }

    private Optional<Content> findContent(String slug) {
        for (Content content : contents.keySet()) {
            if (content.slug().equals(slug))
                return Optional.of(content);
        }
        return Optional.empty();
    }

    public void removeContent(String slug) throws IOException {
        Optional<Content> content = findContent(slug);
        if (content.isPresent())
            removeContent(content.get());
    }

    /**
     * {@link #downloadContents()} and update contents.
     * Save the instance after this update.
     *
     * @throws IOException If an I/O error occurs
     */
    public synchronized void updateContents() throws IOException {
        downloadContents();
        Mimo.LOGGER.info("[{}] Updating contents", this);
        RequestCollector.Distributor<Content.Version> distributor = collector.submit(
                RequestCollector.Type.MULTIPLE_LATEST_VERSION,
                RequestCollector.multipleLatestVersion(this)
        );
        for (ContentInstance contentInstance : contents.values()) {
            contentInstance.setLatestVersion(distributor);
            if (contentInstance.isUpToDate())
                continue;

            contentInstance.downloadContent();
        }
        save();
    }

    public synchronized void downloadContents() throws IOException {
        Mimo.LOGGER.info("[{}] Downloading contents", this);
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
        Mimo.LOGGER.debug("[{}] Saving instance", this);
        try (JsonWriter jsonWriter = new JsonWriter(new FileWriter(path.toFile()))) {
            jsonWriter.setFormattingStyle(FormattingStyle.PRETTY);
            Mimo.GSON.toJson(this, ServerInstance.class, jsonWriter);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
