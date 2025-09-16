package com.lalaalal.mimo;

import com.google.gson.annotations.SerializedName;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.json.FieldStrategy;
import com.lalaalal.mimo.json.GsonExcludeStrategy;
import com.lalaalal.mimo.json.GsonField;
import com.lalaalal.mimo.json.TypeStrategy;
import com.lalaalal.mimo.modrinth.ModrinthHelper;
import com.lalaalal.mimo.modrinth.Request;
import com.lalaalal.mimo.modrinth.ResponseParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
        for (Content.Dependency dependency : version.dependencies()) {
            if (dependency.required()) {
                Content content = ModrinthHelper.get(
                        Request.project(dependency.id()),
                        ResponseParser.contentParser(serverInstance)
                );
                serverInstance.addContent(content);
            }
        }
    }

    protected void loadVersions() {
        Mimo.LOGGER.debug("Loading versions for \"%s\"".formatted(content.slug()));
        this.availableVersions = ModrinthHelper.get(
                Request.projectVersions(content, serverInstance),
                ResponseParser::parseProjectVersionList
        );
        if (contentVersion == null)
            selectContentVersion(0);
    }

    public void loadLatestVersion() {
        Mimo.LOGGER.debug("Loading latest version for \"%s\"".formatted(content.slug()));
        updatingVersion = ModrinthHelper.get(
                Request.latestVersion(content, contentVersion, serverInstance),
                ResponseParser::parseVersion
        );
        Mimo.LOGGER.debug("Latest version for \"%s\" is \"%s\"".formatted(content.slug(), updatingVersion.fileName()));
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
        loadLatestVersion();
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
        if (availableVersions.isEmpty())
            throw new IllegalStateException("No available version for %s [%s] [%s]".formatted(content.slug(), serverInstance.loader, serverInstance.version));
        this.contentVersion = availableVersions.get(index);
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

    private Path createContentPath(Content.Version version) throws IOException {
        Path contentPath = Path.of(serverInstance.path.toString(), content.type().path, version.fileName());
        Path directory = contentPath.getParent();
        if (!Files.exists(directory)) {
            Mimo.LOGGER.debug("Creating directory \"%s\"".formatted(directory));
            Files.createDirectories(directory);
        }
        return contentPath;
    }

    public void downloadContent() throws IOException {
        removeContent();
        Content.Version version = getDownloadingVersion();
        Path contentPath = createContentPath(version);
        Files.createDirectories(contentPath.getParent());
        Mimo.LOGGER.info("Downloading \"%s\"".formatted(contentPath));
        ModrinthHelper.download(version, contentPath);
        handlePostDownloadUpdatingVersion();
    }

    public void removeContent() throws IOException {
        Content.Version version = getContentVersion();
        Path contentPath = createContentPath(version);
        Mimo.LOGGER.info("Deleting \"%s\"".formatted(contentPath));
        Files.deleteIfExists(contentPath);
    }

    @Override
    public String toString() {
        if (contentVersion == null)
            return content.slug();
        return content.slug() + " (" + contentVersion.fileName() + ")";
    }
}
