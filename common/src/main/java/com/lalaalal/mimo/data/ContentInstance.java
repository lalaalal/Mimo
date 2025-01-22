package com.lalaalal.mimo.data;

import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.modrinth.ModrinthHelper;
import com.lalaalal.mimo.modrinth.Request;
import com.lalaalal.mimo.modrinth.Response;
import com.lalaalal.mimo.modrinth.ResponseHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class ContentInstance {
    private final ServerInstance serverInstance;
    private final Content content;
    private List<Content.Version> availableVersions;
    private Content.Version contentVersion;
    private Content.Version updatingVersion;
    private Thread versionLoadingThread;

    public ContentInstance(ServerInstance serverInstance, Content content) {
        this.serverInstance = serverInstance;
        this.content = content;
        loadVersionsInThread(instance -> {
        });
    }

    protected void storeLoadedVersions(Response response) {
        this.availableVersions = ResponseHandler.resolveVersionDataList(response);
    }

    protected void loadVersionsInThread(Consumer<ContentInstance> callback) {
        if (versionLoadingThread != null && versionLoadingThread.isAlive())
            return;
        versionLoadingThread = ModrinthHelper.createRequestThread(
                Request.versions(content.slug(), serverInstance),
                response -> {
                    this.availableVersions = ResponseHandler.resolveVersionDataList(response);
                    callback.accept(this);
                }
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
        ModrinthHelper.sendRequest(
                Request.versions(content.slug(), serverInstance),
                this::storeLoadedVersions
        );
    }

    public void loadLatestVersion(Consumer<ContentInstance> callback) {
        ModrinthHelper.createRequestThread(
                Request.latestVersion(contentVersion, serverInstance),
                response -> {
                    updatingVersion = ResponseHandler.resolveVersionData(response);
                    callback.accept(this);
                }
        ).start();
    }

    public boolean isUpToDate() {
        if (!isVersionSelected())
            return true;
        if (updatingVersion == null)
            ModrinthHelper.sendRequest(Request.latestVersion(contentVersion, serverInstance),
                    response -> updatingVersion = ResponseHandler.resolveVersionData(response)
            );
        return contentVersion.id().equals(updatingVersion.id());
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

    public void selectContentVersion(int index) {
        if (availableVersions == null)
            loadVersions();
        if (availableVersions.isEmpty())
            throw new IllegalStateException("No available version for %s [%s] [%s]".formatted(content.slug(), serverInstance.loader, serverInstance.version));
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
