package com.lalaalal.mimo.content_provider;

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
    public List<Content.Version> getProjectVersions(Content content, ServerInstance serverInstance) {
        return List.of();
    }

    @Override
    public Content.Version getLatestVersion(Content content, Content.Version version, ServerInstance serverInstance) {
        return version;
    }

    @Override
    public Map<String, Content.Detail> search(String name) {
        return Map.of();
    }

    @Override
    public Map<Content, Content.Version> getLatestVersions(Map<String, File> hashes, ServerInstance serverInstance) {
        return Map.of();
    }
}
