package com.lalaalal.mimo;

import com.google.gson.annotations.SerializedName;
import com.lalaalal.mimo.content_provider.ContentProvider;
import com.lalaalal.mimo.content_provider.CustomContentProvider;
import com.lalaalal.mimo.content_provider.RequestCollector;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.json.FieldStrategy;
import com.lalaalal.mimo.json.GsonExcludeStrategy;
import com.lalaalal.mimo.json.GsonField;
import com.lalaalal.mimo.json.TypeStrategy;
import com.lalaalal.mimo.logging.ComplexMessageComponent;
import com.lalaalal.mimo.logging.ConsoleColor;
import com.lalaalal.mimo.logging.MessageComponent;
import com.lalaalal.mimo.logging.Style;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Instance of a content.
 * Manages the content and its versions with dependencies.
 */
@GsonExcludeStrategy(TypeStrategy.INCLUDE_MARKED)
public class ContentInstance {
    private final ServerInstance serverInstance;

    @GsonField(FieldStrategy.INCLUDE)
    private final Content content;
    private List<Content.Version> availableVersions;

    private final ContentProvider contentProvider;

    @GsonField(FieldStrategy.INCLUDE)
    @SerializedName("version")
    private Content.Version contentVersion;
    private Content.Version updatingVersion;

    public ContentInstance(ServerInstance serverInstance, Content content) {
        this.serverInstance = serverInstance;
        this.content = content;
        this.contentProvider = content.provider();

        loadVersions();
    }

    protected ContentInstance(ServerInstance serverInstance, Content content, Content.Version version) {
        this.serverInstance = serverInstance;
        this.content = content;
        this.contentVersion = version;
        this.contentProvider = content.provider();
    }

    protected void resolveDependencies(Content.Version version) {
        Mimo.LOGGER.info("[{}] ({}) Resolving dependencies for \"{}\"", serverInstance, this, version.fileName());
        for (Content.Dependency dependency : version.dependencies()) {
            if (dependency.required()) {
                Content content = contentProvider.getContentWithId(dependency.id(), serverInstance);
                serverInstance.addContent(content);
            } else {
                Mimo.LOGGER.debug("[{}] ({}) Skipping optional dependency \"{}\"", serverInstance, this, dependency.id());
            }
        }
    }

    protected void loadVersions() {
        Mimo.LOGGER.debug("[{}] ({}) Loading versions for \"{}\"", serverInstance, this, content.slug());
        this.availableVersions = contentProvider.getSingleVersions(content, serverInstance);
        if (contentVersion == null)
            selectContentVersion(0);
    }

    public void setLatestVersion(RequestCollector.Distributor<Content.Version> distributor) {
        if (distributor.is(RequestCollector.Type.MULTIPLE_LATEST_VERSION)) {
            distributor.getResult(content.id())
                    .ifPresent(version -> this.updatingVersion = version);
        }
    }

    public void checkLatestVersion() {
        if (updatingVersion == null)
            loadLatestVersion();
    }

    public void loadLatestVersion() {
        Mimo.LOGGER.debug("[{}] ({}) Loading latest version", serverInstance, this);
        updatingVersion = contentProvider.getLatestVersion(this, serverInstance);
        Mimo.LOGGER.debug("[{}] ({}) Latest version is \"{}\"", serverInstance, this, content.slug());
    }

    public boolean is(Content content) {
        return this.content.equals(content);
    }

    public boolean isCustom() {
        return contentProvider == CustomContentProvider.INSTANCE;
    }

    public Content content() {
        return content;
    }

    public boolean isUpToDate() {
        if (!isVersionSelected())
            return false;
        checkLatestVersion();
        return contentVersion.versionId().equals(updatingVersion.versionId());
    }

    public MinecraftVersion getMinecraftVersion() {
        return serverInstance.version;
    }

    public Content.Version getContentVersion() {
        return contentVersion;
    }

    public Content.Version getUpdatingVersion() {
        return updatingVersion;
    }

    public List<Content.Version> getAvailableVersions() {
        if (availableVersions == null || availableVersions.isEmpty())
            loadVersions();

        return availableVersions;
    }

    public void setContentVersion(Content.Version version) {
        this.contentVersion = version;
    }

    public void selectContentVersion(int index) {
        if (availableVersions == null)
            loadVersions();
        if (availableVersions.isEmpty()) {
            Mimo.LOGGER.warning("[{}] ({}) No available version", serverInstance, this);
            throw new IllegalStateException("Aborted");
        }
        this.contentVersion = availableVersions.get(index);
        Mimo.LOGGER.info("[{}] ({}) Selecting version \"{}\"", serverInstance, this, contentVersion.fileName());
        resolveDependencies(contentVersion);
    }

    public boolean isVersionSelected() {
        return this.contentVersion != null;
    }

    private Content.Version getDownloadingVersion() {
        if (updatingVersion != null)
            return updatingVersion;
        if (!isVersionSelected())
            selectContentVersion(0);
        return contentVersion;
    }

    private void handlePostDownloadUpdatingVersion() {
        if (updatingVersion != null)
            contentVersion = updatingVersion;
    }

    private Path getContentPath(Content.Version version) {
        return Path.of(serverInstance.path.toString(), content.type().path, version.fileName());
    }

    private Path createContentPath(Content.Version version) throws IOException {
        Path contentPath = getContentPath(version);
        Path directory = contentPath.getParent();
        if (!Files.exists(directory)) {
            Mimo.LOGGER.debug("[{}] ({}) Creating directory \"{}\"", serverInstance, this, directory);
            Files.createDirectories(directory);
        }
        return contentPath;
    }

    public void downloadContent() throws IOException {
        if (isCustom()) {
            Mimo.LOGGER.warning("[{}] ({}) Skip downloading custom content", serverInstance, this);
            return;
        }
        removeContent();
        Content.Version version = getDownloadingVersion();
        Path contentPath = createContentPath(version);
        Files.createDirectories(contentPath.getParent());
        Mimo.LOGGER.info("[{}] ({}) Downloading \"{}\"", serverInstance, this, contentPath);
        contentProvider.download(version, contentPath);
        handlePostDownloadUpdatingVersion();
    }

    public void removeContent() throws IOException {
        Content.Version version = getContentVersion();
        Path contentPath = getContentPath(version);
        if (Files.exists(contentPath)) {
            Mimo.LOGGER.info("[{}] ({}) Deleting \"{}\"", serverInstance, this, contentPath);
            Files.delete(contentPath);
        }
    }

    public boolean isDownloaded() {
        Path contentPath = getContentPath(getContentVersion());
        return Files.exists(contentPath);
    }

    @Override
    public String toString() {
        return content.slug();
    }

    public MessageComponent getStyledText() {
        String name = content.slug();
        if (isVersionSelected())
            name += " (" + contentVersion.fileName() + ")";
        ComplexMessageComponent component = MessageComponent.complex(MessageComponent.withDefault(name));
        component.add(MessageComponent.text(" " + content.provider().getName()).with(providerStyle()));
        if (!isDownloaded())
            component.add(MessageComponent.text(" NOT DOWNLOADED").with(ConsoleColor.RED.foreground()));
        if (!isUpToDate())
            component.add(MessageComponent.text(" OUT OF DATE").with(ConsoleColor.YELLOW.foreground()));
        return component;
    }

    private Style providerStyle() {
        if (content.provider() == CustomContentProvider.INSTANCE)
            return ConsoleColor.YELLOW.foreground();
        return ConsoleColor.GREEN.foreground();
    }
}
