package com.lalaalal.mimo;

import com.google.gson.annotations.SerializedName;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ContentDetail;
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
import java.util.function.Consumer;

@GsonExcludeStrategy(TypeStrategy.INCLUDE_MARKED)
public class ContentInstance {
    private final ServerInstance serverInstance;

    @GsonField(FieldStrategy.INCLUDE)
    private final Content content;
    private ContentDetail detail;
    private List<Content.Version> availableVersions;
    private List<Content> dependencies;

    @GsonField(FieldStrategy.INCLUDE)
    @SerializedName("version")
    private Content.Version contentVersion;
    private Content.Version updatingVersion;
    private Thread versionLoadingThread;

    public ContentInstance(ServerInstance serverInstance, Content content) {
        this.serverInstance = serverInstance;
        this.content = content;
        this.dependencies = List.of();

        loadVersionsInThread();
        resolveDependencies();
    }

    protected ContentInstance(ServerInstance serverInstance, Content content, Content.Version version) {
        this.serverInstance = serverInstance;
        this.content = content;
        this.dependencies = List.of();
        this.contentVersion = version;
    }

    protected void resolveDependencies() {
        this.dependencies = ModrinthHelper.get(Request.dependencies(content.id()), ResponseParser::parseDependencies);
        for (Content dependency : dependencies)
            serverInstance.addContent(dependency);
    }

    protected void loadVersionsInThread() {
        if (versionLoadingThread != null && versionLoadingThread.isAlive())
            return;
        versionLoadingThread = ModrinthHelper.createRequestThread(
                Request.projectVersions(content.id(), serverInstance),
                response -> this.availableVersions = ResponseParser.parseProjectVersionList(response)
        );
        versionLoadingThread.start();
    }

    protected void loadVersions() {
        if (versionLoadingThread != null && versionLoadingThread.isAlive()) {
            try {
                versionLoadingThread.join();
                return;
            } catch (InterruptedException ignored) {

            }
        }
        this.availableVersions = ModrinthHelper.get(
                Request.projectVersions(content.id(), serverInstance),
                ResponseParser::parseProjectVersionList
        );
    }

    public void loadLatestVersionInThread(Consumer<ContentInstance> callback) {
        ModrinthHelper.createRequestThread(
                Request.latestVersion(contentVersion, serverInstance),
                response -> {
                    updatingVersion = ResponseParser.parseVersion(response);
                    callback.accept(this);
                }
        ).start();
    }

    public void loadLatestVersion() {
        updatingVersion = ModrinthHelper.get(
                Request.latestVersion(contentVersion, serverInstance),
                ResponseParser::parseVersion
        );
    }

    public boolean is(Content content) {
        return this.content.equals(content);
    }

    public boolean isUpToDate() {
        if (!isVersionSelected())
            return true;
        if (updatingVersion == null)
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
            throw new IllegalStateException("No available version for %s [%s] [%s]".formatted(content.id(), serverInstance.loader, serverInstance.version));
        this.contentVersion = availableVersions.get(index);
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
        Files.createDirectories(directory);
        return contentPath;
    }

    public void downloadContent() throws IOException {
        Content.Version version = getDownloadingVersion();
        Path contentPath = createContentPath(version);
        ModrinthHelper.download(version, contentPath);
        handlePostDownloadUpdatingVersion();
    }
}
