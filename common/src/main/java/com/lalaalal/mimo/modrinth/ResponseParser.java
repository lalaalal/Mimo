package com.lalaalal.mimo.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ProjectType;
import com.lalaalal.mimo.json.JsonHelper;
import com.lalaalal.mimo.loader.Loader;

import java.util.*;
import java.util.function.Function;

/**
 * Provides methods for parsing {@link Response} from Modrinth API.
 */
public class ResponseParser {
    protected static void verifyRequestType(Response response, Request.Type... requiredTypes) {
        verifyRequestType(response, List.of(requiredTypes));
    }

    protected static void verifyRequestType(Response response, List<Request.Type> requiredTypes) {
        Mimo.LOGGER.debug("[REQ %03d] Verifying request type for %s".formatted(response.id(), response));
        if (!requiredTypes.contains(response.requestType()))
            throw new IllegalArgumentException("[REQ %03d] Required request type is [%s] but %s".formatted(response.id(), requiredTypes, response.requestType()));
    }

    public static Function<Response, Content> contentParser(ServerInstance serverInstance) {
        return response -> parseContent(serverInstance.loader.type(), response);
    }

    public static Function<Response, List<Content>> contentListParser(ServerInstance serverInstance) {
        return response -> parseContentList(serverInstance.loader.type(), response);
    }

    public static List<Content> parseContentList(Loader.Type loader, Response response) {
        verifyRequestType(response, Request.Type.GET_PROJECT_LIST);
        logStartParsing("content list", response);
        JsonArray list = JsonHelper.toJsonArray(response.data());
        return result(response, parseContentListFromJsonArray(loader, list));
    }

    public static Content parseContent(Loader.Type loader, Response response) {
        verifyRequestType(response, Request.Type.GET_PROJECT);
        logStartParsing("content", response);
        return result(response, parseContent(loader, JsonHelper.toJsonObject(response.data())));
    }

    public static Map<String, Content.Detail> parseSearchData(Response response) {
        verifyRequestType(response, Request.Type.SEARCH);
        logStartParsing("search data", response);
        JsonObject data = JsonHelper.toJsonObject(response.data());
        JsonHelper.testKeys(data, "hits");
        JsonArray hits = JsonHelper.toJsonArray(data.get("hits"));
        return result(response, parseDetailMap(hits));
    }

    public static List<Content.Version> parseProjectVersionList(Response response) {
        verifyRequestType(response, Request.Type.GET_PROJECT_VERSION_LIST);
        logStartParsing("version list", response);
        List<Content.Version> versions = new ArrayList<>();
        for (JsonElement element : JsonHelper.toJsonArray(response.data()))
            versions.add(parseVersion(element));

        return result(response, versions);
    }

    public static Content.Version parseVersion(Response response) {
        verifyRequestType(response, Request.Type.LATEST_VERSION, Request.Type.GET_VERSION_FILE);
        logStartParsing("version", response);
        return result(response, parseVersion(response.data()));
    }

    public static Map<String, Content.Version> parseVersionListWithProjectId(Response response) {
        verifyRequestType(response, Request.Type.GET_VERSION_FILE_LIST);
        logStartParsing("version mapping", response);
        JsonObject data = JsonHelper.toJsonObject(response.data());
        Map<String, Content.Version> versions = new HashMap<>();
        for (String key : data.keySet()) {
            JsonObject element = JsonHelper.toJsonObject(data.get(key));
            versions.put(parseProjectId(element), parseVersion(element));
        }
        return result(response, versions);
    }

    public static String parseProjectId(Response response) {
        verifyRequestType(response, Request.Type.LATEST_VERSION, Request.Type.GET_PROJECT, Request.Type.GET_VERSION_FILE);
        logStartParsing("project id", response);
        return result(response, parseProjectId(response.data()));
    }

    private static ProjectType parseProjectType(JsonObject data) {
        if (!data.has("loaders"))
            return parsed("project type", ProjectType.get(JsonHelper.toString(data.get("project_type"))));
        List<String> loaders = JsonHelper.toJsonArray(data.get("loaders"))
                .asList()
                .stream()
                .map(JsonHelper::toString)
                .toList();
        if (loaders.contains("datapack") && loaders.size() == 1)
            return parsed("project type", ProjectType.DATAPACK);
        return parsed("project type", ProjectType.MOD);
    }

    private static Content parseContent(Loader.Type loader, JsonObject data) {
        String projectIdKey = data.has("project_id") ? "project_id" : "id";
        JsonHelper.testKeys(data, projectIdKey, "slug", "project_type");

        String id = JsonHelper.toString(data.get(projectIdKey));
        String slug = JsonHelper.toString(data.get("slug"));
        ProjectType projectType = parseProjectType(data);
        if (data.has("server_side") && JsonHelper.toString(data.get("server_side")).equals("unsupported"))
            Mimo.LOGGER.warning("Content \"%s\" is client only!!".formatted(slug));

        return parsed("content", new Content(projectType, loader, id, slug));
    }

    private static List<Content> parseContentListFromJsonArray(Loader.Type loader, JsonArray list) {
        List<Content> contents = new ArrayList<>();
        for (JsonElement element : list) {
            JsonObject currentContent = JsonHelper.toJsonObject(element);
            Content content = parseContent(loader, currentContent);
            contents.add(content);
        }
        return parsed("content list", contents);
    }

    private static Map<String, Content.Detail> parseDetailMap(JsonArray list) {
        Map<String, Content.Detail> contents = new LinkedHashMap<>();
        for (JsonElement element : list) {
            String slug = parseSlug(element);
            Content.Detail detail = parseDetail(element);
            contents.put(slug, detail);
        }
        return parsed("slug list", contents);
    }

    private static Content.Detail parseDetail(JsonElement element) {
        JsonObject detailData = JsonHelper.toJsonObject(element);
        JsonHelper.testKeys(detailData, "title", "description");
        String title = JsonHelper.toString(detailData.get("title"));
        String description = JsonHelper.toString(detailData.get("description"));
        return parsed("detail", new Content.Detail(title, description));
    }

    private static Content.Version parseVersion(JsonElement element) {
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

    private static String parseProjectId(JsonElement element) {
        JsonObject data = JsonHelper.toJsonObject(element);
        String projectIdKey = data.has("project_id") ? "project_id" : "id";
        JsonHelper.testKeys(data, projectIdKey);
        return parsed("project id", JsonHelper.toString(data.get(projectIdKey)));
    }

    private static String parseSlug(JsonElement element) {
        JsonObject data = JsonHelper.toJsonObject(element);
        JsonHelper.testKeys(data, "slug");
        return parsed("slug", JsonHelper.toString(data.get("slug")));
    }

    private static void logStartParsing(String target, Response response) {
        Mimo.LOGGER.debug("[REQ %03d] Starting parsing %s for %s".formatted(response.id(), target, response.requestType()));
    }

    private static <T> T parsed(String name, T value) {
        Mimo.LOGGER.verbose("Parsed %s is \"%s\"".formatted(name, value));
        return value;
    }

    private static <T> T result(Response response, T value) {
        Mimo.LOGGER.debug("[REQ %03d] Parse result for %s is \"%s\"".formatted(response.id(), response.requestType(), value));
        return value;
    }
}
