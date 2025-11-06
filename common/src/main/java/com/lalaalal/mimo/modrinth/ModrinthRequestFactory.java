package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.content_provider.Request;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ContentFilter;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.loader.Loader;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ModrinthRequestFactory {
    public static final ModrinthRequestFactory INSTANCE = new ModrinthRequestFactory();

    private static final Request.Format SEARCH = new Request.Format(Request.Type.SEARCH, Request.QueryMaker.QUERY_PARAM, "search");
    private static final Request.Format GET_SINGLE_PROJECT = new Request.Format(Request.Type.GET_SINGLE_PROJECT, Request.QueryMaker.PATH_PARAM, "project/${id}");
    private static final Request.Format GET_MULTIPLE_PROJECT = new Request.Format(Request.Type.GET_MULTIPLE_PROJECT, Request.QueryMaker.QUERY_PARAM, "projects");
    private static final Request.Format GET_SINGLE_VERSION_LIST_FROM_ID = new Request.Format(Request.Type.GET_SINGLE_VERSION_LIST, Request.QueryMaker.MIXED, "project/${id}/version");
    private static final Request.Format GET_SINGLE_VERSION_LIST_FROM_HASH = new Request.Format(Request.Type.GET_SINGLE_VERSION_LIST, Request.QueryMaker.PATH_PARAM, "version_file/${hash}");
    private static final Request.Format GET_SINGLE_LATEST_VERSION_FROM_HASH = new Request.Format(Request.Type.GET_SINGLE_VERSION, Request.QueryMaker.PATH_PARAM, "version_file/${hash}/update");
    private static final Request.Format GET_MULTIPLE_VERSION_LIST_FROM_HASH = new Request.Format(Request.Type.GET_MULTIPLE_VERSION_LIST, Request.QueryMaker.EXACT, "version_files");
    private static final Request.Format GET_MULTIPLE_LATEST_VERSION_FROM_HASH = new Request.Format(Request.Type.GET_MULTIPLE_VERSION, Request.QueryMaker.EXACT, "version_files/update");

    private ModrinthRequestFactory() {

    }

    public Request search(String name, ContentFilter filter) {
        return new Request(SEARCH, Map.of("query", name, "facets", filter.toString()));
    }

    public Request singleProject(String id) {
        return new Request(GET_SINGLE_PROJECT, Map.of("${id}", id));
    }

    public Request multipleProject(Collection<String> ids) {
        return new Request(GET_MULTIPLE_PROJECT, Map.of("ids", Mimo.GSON.toJson(ids)));
    }

    public Request singleVersionFromHash(String hash) {
        return new Request(GET_SINGLE_VERSION_LIST_FROM_HASH, Map.of("${hash}", hash));
    }

    public Request multipleVersionListFromHash(Collection<String> hashes) {
        String body = Mimo.GSON.toJson(
                Map.of("hashes", hashes, "algorithm", "sha1"));
        return new Request(GET_MULTIPLE_VERSION_LIST_FROM_HASH, Map.of(), body);
    }

    public Request multipleLatestVersionHash(Collection<String> hashes, ServerInstance instance) {
        String body = Mimo.GSON.toJson(Map.of(
                "hashes", hashes,
                "algorithm", "sha1",
                "loaders", List.of(instance.loader.type().toString()),
                "game_versions", List.of(instance.version.name())
        ));
        return new Request(GET_MULTIPLE_LATEST_VERSION_FROM_HASH, Map.of(), body);
    }

    public Request singleVersionList(String slug, MinecraftVersion version, Loader.Type loader) {
        return new Request(GET_SINGLE_VERSION_LIST_FROM_ID,
                Map.of(
                        "${id}", slug,
                        "loaders", "[\"%s\"]".formatted(loader),
                        "game_versions", "[\"%s\"]".formatted(version)
                )
        );
    }

    public Request singleVersionList(Content content, ServerInstance instance) {
        return singleVersionList(content.slug(), instance.version, content.loader());
    }

    public Request singleLatestVersion(Content.Version version, ServerInstance instance) {
        String body = Mimo.GSON.toJson(Map.of(
                "loaders", new String[]{instance.loader.type().toString()},
                "game_versions", new String[]{instance.version.toString()}
        ));
        return new Request(GET_SINGLE_LATEST_VERSION_FROM_HASH,
                Map.of("${hash}", version.hash()),
                body
        );
    }
}
