package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.ContentInstance;
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
        return get(factory.singleProject(slug), parser.contentParser(serverInstance));
    }

    @Override
    public Content getContentWithId(String id, ServerInstance serverInstance) {
        return getContentWithSlug(id, serverInstance);
    }

    @Override
    public List<Content.Version> getSingleVersions(Content content, ServerInstance serverInstance) {
        return get(factory.singleVersionList(content, serverInstance), parser::parseProjectVersionList);
    }

    @Override
    public Content.Version getLatestVersion(ContentInstance contentInstance, ServerInstance serverInstance) {
        return forgetAndGet(factory.singleLatestVersion(contentInstance.getContentVersion(), serverInstance), parser::parseVersion);
    }

    @Override
    public Map<String, Content.Version> getMultipleLatestVersion(List<ContentInstance> contents, ServerInstance serverInstance) {
        List<String> hashes = contents.stream()
                .map(contentInstance -> contentInstance.getContentVersion().hash())
                .toList();
        return get(factory.multipleLatestVersionHash(hashes, serverInstance), parser::parseVersionMapFromHash);
    }

    @Override
    public Map<Content, Content.Version> getVersionFromFiles(Map<String, File> hashes, ServerInstance serverInstance) {
        Map<String, Content.Version> versions = get(factory.multipleVersionListFromHash(hashes.keySet()), parser::parseVersionMapFromHash);
        List<Content> contents = get(factory.multipleProject(versions.keySet()), parser.contentListParser(serverInstance));
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
