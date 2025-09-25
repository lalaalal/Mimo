package com.lalaalal.mimo;

import com.google.gson.annotations.SerializedName;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.json.FieldStrategy;
import com.lalaalal.mimo.json.GsonExcludeStrategy;
import com.lalaalal.mimo.json.GsonField;
import com.lalaalal.mimo.json.TypeStrategy;
import com.lalaalal.mimo.logging.ComplexMessageComponent;
import com.lalaalal.mimo.logging.ConsoleColor;
import com.lalaalal.mimo.logging.MessageComponent;
import com.lalaalal.mimo.modrinth.ModrinthHelper;
import com.lalaalal.mimo.modrinth.Request;
import com.lalaalal.mimo.modrinth.ResponseParser;

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

    @SerializedName("version") @GsonField(FieldStrategy.INCLUDE)
    private Content.Version contentVersion;
    private Content.Version updatingVersion;

    public ContentInstance(ServerInstance serverInstance, Content content) {
        this.serverInstance = serverInstance;
        this.content = content;

        loadVersions();
    }

    protected ContentInstance(ServerInstance serverInstance, Content content, Content.Version version) {
        this.serverInstance = serverInstance;
        this.content = content;
        this.contentVersion = version;
    }

    protected void resolveDependencies(Content.Version version) {
        Mimo.LOGGER.info("[%s] (%s) Resolving dependencies for \"%s\"".formatted(serverInstance, this, version.fileName()));
        for (Content.Dependency dependency : version.dependencies()) {
            if (dependency.required()) {
                Content content = ModrinthHelper.get(
                        Request.project(dependency.id()),
                        ResponseParser.contentParser(serverInstance)
                );
                serverInstance.addContent(content);
            } else {
                Mimo.LOGGER.debug("[%s] (%s) Skipping optional dependency \"%s\"".formatted(serverInstance, this, dependency.id()));
            }
        }
    }

    protected void loadVersions() {
        Mimo.LOGGER.debug("[%s] (%s) Loading versions for \"%s\"".formatted(serverInstance, this, content.slug()));
        this.availableVersions = ModrinthHelper.get(
                Request.projectVersions(content, serverInstance),
                ResponseParser::parseProjectVersionList
        );
        if (contentVersion == null)
            selectContentVersion(0);
    }

    public void checkLatestVersion() {
        if (updatingVersion == null)
            loadLatestVersion();
    }

    public void loadLatestVersion() {
        Mimo.LOGGER.debug("[%s] (%s) Loading latest version".formatted(serverInstance, this));
        updatingVersion = ModrinthHelper.get(
                Request.latestVersion(content, contentVersion, serverInstance),
                ResponseParser::parseVersion
        );
        Mimo.LOGGER.debug("[%s] (%s) Latest version is \"%s\"".formatted(serverInstance, this, content.slug()));
    }

    public boolean is(Content content) {
        return this.content.equals(content);
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
            Mimo.LOGGER.warning("[%s] (%s) No available version".formatted(serverInstance, this));
            throw new IllegalStateException("Aborted");
        }
        this.contentVersion = availableVersions.get(index);
        Mimo.LOGGER.info("[%s] (%s) Selecting version \"%s\"".formatted(serverInstance, this, contentVersion.fileName()));
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
            Mimo.LOGGER.debug("[%s] (%s) Creating directory \"%s\"".formatted(serverInstance, this, directory));
            Files.createDirectories(directory);
        }
        return contentPath;
    }

    public void downloadContent() throws IOException {
        removeContent();
        Content.Version version = getDownloadingVersion();
        Path contentPath = createContentPath(version);
        Files.createDirectories(contentPath.getParent());
        Mimo.LOGGER.info("[%s] (%s) Downloading \"%s\"".formatted(serverInstance, this, contentPath));
        ModrinthHelper.download(version, contentPath);
        handlePostDownloadUpdatingVersion();
    }

    public void removeContent() throws IOException {
        Content.Version version = getContentVersion();
        Path contentPath = getContentPath(version);
        if (Files.exists(contentPath)) {
            Mimo.LOGGER.info("[%s] (%s) Deleting \"%s\"".formatted(serverInstance, this, contentPath));
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
        if (!isDownloaded())
            component.add(MessageComponent.text(" NOT DOWNLOADED").with(ConsoleColor.RED.foreground()));
        if (!isUpToDate())
            component.add(MessageComponent.text(" OUT OF DATE").with(ConsoleColor.YELLOW.foreground()));
        return component;
    }
}
