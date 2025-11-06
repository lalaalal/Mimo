package com.lalaalal.mimo.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.content_provider.Response;
import com.lalaalal.mimo.content_provider.ResponseParser;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ProjectType;
import com.lalaalal.mimo.json.JsonHelper;
import com.lalaalal.mimo.loader.Loader;

import java.util.*;
import java.util.function.Function;

/**
 * Provides methods for parsing {@link Response} from Modrinth API.
 */
public class ModrinthResponseParser extends ResponseParser {
    public static final ModrinthResponseParser INSTANCE = new ModrinthResponseParser();

    private ModrinthResponseParser() {

    }

    public Function<Response, Content> contentParser(ServerInstance serverInstance) {
        return response -> parseContent(serverInstance.loader.type(), response);
    }

    public Function<Response, List<Content>> contentListParser(ServerInstance serverInstance) {
        return response -> parseContentList(serverInstance.loader.type(), response);
    }

    public List<Content> parseContentList(Loader.Type loader, Response response) {
        logStartParsing("content list", response);
        JsonArray list = JsonHelper.toJsonArray(response.data());
        return result(response, parseContentListFromJsonArray(loader, list));
    }

    public Content parseContent(Loader.Type loader, Response response) {
        logStartParsing("content", response);
        return result(response, parseContent(loader, JsonHelper.toJsonObject(response.data())));
    }

    public List<Content.Version> parseProjectVersionList(Response response) {
        logStartParsing("version list", response);
        List<Content.Version> versions = new ArrayList<>();
        for (JsonElement element : JsonHelper.toJsonArray(response.data()))
            versions.add(parseVersion(element));

        return result(response, versions);
    }

    public Content.Version parseVersion(Response response) {
        logStartParsing("version", response);
        return result(response, parseVersion(response.data()));
    }

    public Map<String, Content.Version> parseVersionMapFromHash(Response response) {
        logStartParsing("version mapping", response);
        JsonObject data = JsonHelper.toJsonObject(response.data());
        Map<String, Content.Version> versions = new HashMap<>();
        for (String key : data.keySet()) {
            JsonObject element = JsonHelper.toJsonObject(data.get(key));
            versions.put(parseProjectId(element), parseVersion(element));
        }
        return result(response, versions);
    }

    public Map<String, Content.Detail> parseSearchData(Response response) {
        logStartParsing("search data", response);
        JsonObject data = JsonHelper.toJsonObject(response.data());
        JsonHelper.testKeys(data, "hits");
        JsonArray hits = JsonHelper.toJsonArray(data.get("hits"));
        return result(response, parseDetailMap(hits));
    }

    protected ProjectType parseProjectType(JsonObject data) {
        if (!data.has("loaders"))
            return parsed("project format", ProjectType.get(JsonHelper.toString(data.get("project_type"))));
        List<String> loaders = JsonHelper.toJsonArray(data.get("loaders"))
                .asList()
                .stream()
                .map(JsonHelper::toString)
                .toList();
        if (loaders.contains("datapack") && loaders.size() == 1)
            return parsed("project format", ProjectType.DATAPACK);
        return parsed("project format", ProjectType.MOD);
    }

    protected Content parseContent(Loader.Type loader, JsonObject data) {
        String projectIdKey = data.has("project_id") ? "project_id" : "id";
        JsonHelper.testKeys(data, projectIdKey, "slug", "project_type");

        String id = JsonHelper.toString(data.get(projectIdKey));
        String slug = JsonHelper.toString(data.get("slug"));
        ProjectType projectType = parseProjectType(data);
        if (data.has("server_side") && JsonHelper.toString(data.get("server_side")).equals("unsupported"))
            Mimo.LOGGER.warning("Content \"{}\" is client only!!", slug);

        return parsed("content", new Content(projectType, loader, ModrinthContentProvider.INSTANCE, id, slug));
    }

    protected Map<String, Content.Detail> parseDetailMap(JsonArray list) {
        Map<String, Content.Detail> contents = new LinkedHashMap<>();
        for (JsonElement element : list) {
            String slug = parseSlug(element);
            Content.Detail detail = parseDetail(element);
            contents.put(slug, detail);
        }
        return parsed("slug list", contents);
    }

    protected Content.Detail parseDetail(JsonElement element) {
        JsonObject detailData = JsonHelper.toJsonObject(element);
        JsonHelper.testKeys(detailData, "title", "description");
        String title = JsonHelper.toString(detailData.get("title"));
        String description = JsonHelper.toString(detailData.get("description"));
        return parsed("detail", new Content.Detail(title, description));
    }

    protected Content.Version parseVersion(JsonElement element) {
        JsonObject versionData = JsonHelper.toJsonObject(element);
        JsonHelper.testKeys(versionData, "id", "files", "dependencies");
        String versionId = JsonHelper.toString(versionData.get("id"));
        JsonArray files = JsonHelper.toJsonArray(versionData.get("files"));
        if (files.isEmpty())
            throw new IllegalStateException("No available version for " + versionId);

        JsonObject file = JsonHelper.toJsonObject(files.get(0));
        JsonHelper.testKeys(file, "hashes", "url", "filename");
        JsonObject hashes = JsonHelper.toJsonObject(file.get("hashes"));
        String hash = JsonHelper.toString(hashes.get("sha1"));
        String url = JsonHelper.toString(file.get("url"));
        String fileName = JsonHelper.toString(file.get("filename"));

        JsonArray dependencies = JsonHelper.toJsonArray(versionData.get("dependencies"));
        List<Content.Dependency> versionDependencies = new ArrayList<>();
        for (JsonElement dependency : dependencies) {
            JsonObject dependencyData = JsonHelper.toJsonObject(dependency);
            JsonHelper.testKeys(dependencyData, "project_id", "dependency_type");
            String dependencyId = JsonHelper.toString(dependencyData.get("project_id"));
            String dependencyType = JsonHelper.toString(dependencyData.get("dependency_type"));
            versionDependencies.add(new Content.Dependency(dependencyId, dependencyType.equals("required")));
        }

        return parsed("version", new Content.Version(versionId, hash, url, fileName, versionDependencies));
    }

    protected String parseProjectId(JsonElement element) {
        JsonObject data = JsonHelper.toJsonObject(element);
        String projectIdKey = data.has("project_id") ? "project_id" : "id";
        JsonHelper.testKeys(data, projectIdKey);
        return parsed("project id", JsonHelper.toString(data.get(projectIdKey)));
    }

    protected String parseSlug(JsonElement element) {
        JsonObject data = JsonHelper.toJsonObject(element);
        JsonHelper.testKeys(data, "slug");
        return parsed("slug", JsonHelper.toString(data.get("slug")));
    }

    protected List<Content> parseContentListFromJsonArray(Loader.Type loader, JsonArray list) {
        List<Content> contents = new ArrayList<>();
        for (JsonElement element : list) {
            JsonObject currentContent = JsonHelper.toJsonObject(element);
            Content content = parseContent(loader, currentContent);
            contents.add(content);
        }
        return parsed("content list", contents);
    }
}
