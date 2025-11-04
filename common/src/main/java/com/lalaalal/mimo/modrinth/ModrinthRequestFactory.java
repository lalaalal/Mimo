package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.content_provider.Request;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ContentFilter;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.loader.Loader;

import java.util.Collection;
import java.util.Map;

public class ModrinthRequestFactory {
    public static final ModrinthRequestFactory INSTANCE = new ModrinthRequestFactory();

    private static final Request.Format SEARCH = new Request.Format(Request.Type.SEARCH, Request.QueryMaker.QUERY_PARAM, "search");
    private static final Request.Format GET_PROJECT = new Request.Format(Request.Type.GET_PROJECT, Request.QueryMaker.PATH_PARAM, "project/${id}");
    private static final Request.Format GET_PROJECT_LIST = new Request.Format(Request.Type.GET_PROJECT_LIST, Request.QueryMaker.QUERY_PARAM, "projects");
    private static final Request.Format GET_PROJECT_DEPENDENCY_LIST = new Request.Format(Request.Type.GET_PROJECT_DEPENDENCY_LIST, Request.QueryMaker.PATH_PARAM, "project/${id}/dependencies");
    private static final Request.Format GET_PROJECT_VERSION_LIST = new Request.Format(Request.Type.GET_PROJECT_VERSION_LIST, Request.QueryMaker.MIXED, "project/${id}/version");
    private static final Request.Format GET_VERSION_FILE = new Request.Format(Request.Type.GET_VERSION_FILE, Request.QueryMaker.PATH_PARAM, "version_file/${hash}");
    private static final Request.Format GET_VERSION_FILE_LIST = new Request.Format(Request.Type.GET_VERSION_FILE_LIST, Request.QueryMaker.EXACT, "version_files");
    private static final Request.Format LATEST_VERSION = new Request.Format(Request.Type.LATEST_VERSION, Request.QueryMaker.PATH_PARAM, "version_file/${hash}/update");

    private ModrinthRequestFactory() {

    }

    public Request search(String name, ContentFilter filter) {
        return new Request(SEARCH, Map.of("query", name, "facets", filter.toString()));
    }

    public Request project(String id) {
        return new Request(GET_PROJECT, Map.of("${id}", id));
    }

    public Request projects(Collection<String> ids) {
        return new Request(GET_PROJECT_LIST, Map.of("ids", Mimo.GSON.toJson(ids)));
    }

    public Request versionFile(String hash) {
        return new Request(GET_VERSION_FILE, Map.of("${hash}", hash));
    }

    public Request versionFiles(Collection<String> hashes) {
        String body = Mimo.GSON.toJson(
                Map.of("hashes", hashes, "algorithm", "sha1"));
        return new Request(GET_VERSION_FILE_LIST, Map.of(), body);
    }

    public Request dependencies(String slug) {
        return new Request(GET_PROJECT_DEPENDENCY_LIST,
                Map.of("${id}", slug)
        );
    }

    public Request projectVersions(String slug, MinecraftVersion version, Loader.Type loader) {
        return new Request(GET_PROJECT_VERSION_LIST,
                Map.of(
                        "${id}", slug,
                        "loaders", "[\"%s\"]".formatted(loader),
                        "game_versions", "[\"%s\"]".formatted(version)
                )
        );
    }

    public Request projectVersions(Content content, ServerInstance instance) {
        return projectVersions(content.slug(), instance.version, content.loader());
    }

    public Request latestVersion(Content.Version version, ServerInstance instance) {
        String body = Mimo.GSON.toJson(Map.of(
                "loaders", new String[]{instance.loader.type().toString()},
                "game_versions", new String[]{instance.version.toString()}
        ));
        return new Request(LATEST_VERSION,
                Map.of("${hash}", version.hash()),
                body
        );
    }
}
