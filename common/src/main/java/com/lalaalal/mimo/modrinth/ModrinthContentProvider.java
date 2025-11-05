package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.content_provider.ContentProvider;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ContentFilter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModrinthContentProvider extends ContentProvider {
    public static final ModrinthContentProvider INSTANCE = new ModrinthContentProvider();

    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/";

    private final ModrinthRequestFactory factory = ModrinthRequestFactory.INSTANCE;
    private final ModrinthResponseParser parser = ModrinthResponseParser.INSTANCE;

    private ModrinthContentProvider() {
        super("modrinth", MODRINTH_API_URL);
    }

    @Override
    public Content getContentWithSlug(String slug, ServerInstance serverInstance) {
        return get(factory.project(slug), parser.contentParser(serverInstance));
    }

    @Override
    public Content getContentWithId(String id, ServerInstance serverInstance) {
        return getContentWithSlug(id, serverInstance);
    }

    @Override
    public List<Content.Version> getProjectVersions(Content content, ServerInstance serverInstance) {
        return get(factory.projectVersions(content, serverInstance), parser::parseProjectVersionList);
    }

    @Override
    public Content.Version getLatestVersion(Content content, Content.Version version, ServerInstance serverInstance) {
        return forgetAndGet(factory.latestVersion(version, serverInstance), parser::parseVersion);
    }

    @Override
    public Map<Content, Content.Version> getLatestVersions(Map<String, File> hashes, ServerInstance serverInstance) {
        Map<String, Content.Version> versions = get(factory.versionFiles(hashes.keySet()), parser::parseVersionsWithProjectId);
        List<Content> contents = get(factory.projects(versions.keySet()), parser.contentListParser(serverInstance));
        Map<Content, Content.Version> result = new HashMap<>();
        for (Content content : contents) {
            Content.Version version = versions.get(content.id());
            result.put(content, version);
        }
        return result;
    }

    @Override
    public Map<String, Content.Detail> search(String name) {
        return get(factory.search(name, ContentFilter.base()), parser::parseSearchData);
    }
}
