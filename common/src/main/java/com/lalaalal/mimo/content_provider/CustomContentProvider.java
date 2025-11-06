package com.lalaalal.mimo.content_provider;

import com.lalaalal.mimo.ContentInstance;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.exception.MessageComponentException;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CustomContentProvider extends ContentProvider {
    public static final CustomContentProvider INSTANCE = new CustomContentProvider();

    private CustomContentProvider() {
        super("custom", "");
    }

    @Override
    public Content getContentWithId(String id, ServerInstance serverInstance) {
        throw new MessageComponentException("Unsupported operation");
    }

    @Override
    public Content getContentWithSlug(String slug, ServerInstance serverInstance) {
        throw new MessageComponentException("Unsupported operation");
    }

    @Override
    public List<Content.Version> getSingleVersions(Content content, ServerInstance serverInstance) {
        return List.of();
    }

    @Override
    public Map<String, Content.Version> getMultipleLatestVersion(List<ContentInstance> contents, ServerInstance serverInstance) {
        return Map.of();
    }

    @Override
    public Content.Version getLatestVersion(ContentInstance contentInstance, ServerInstance serverInstance) {
        return contentInstance.getContentVersion();
    }

    @Override
    public Map<String, Content.Detail> search(String name) {
        return Map.of();
    }

    @Override
    public Map<Content, Content.Version> getVersionFromFiles(Map<String, File> hashes, ServerInstance serverInstance) {
        return Map.of();
    }
}
