package com.lalaalal.mimo.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ProjectType;
import com.lalaalal.mimo.loader.Loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Provides methods for parsing {@link Response} from Modrinth API.
 */
public class ResponseParser {
    protected static void verifyRequestType(Response response, Request.Type... requiredTypes) {
        verifyRequestType(response, List.of(requiredTypes));
    }

    protected static void verifyRequestType(Response response, List<Request.Type> requiredTypes) {
        Mimo.LOGGER.debug("Verifying request type for %s".formatted(response));
        if (!requiredTypes.contains(response.requestType()))
            throw new IllegalArgumentException("Required request type is [%s] but %s".formatted(requiredTypes, response.requestType()));
    }

    public static Function<Response, Content> contentParser(ServerInstance serverInstance) {
        return response -> parseContent(serverInstance.loader.type(), response);
    }

    public static Function<Response, List<Content>> contentListParser(ServerInstance serverInstance) {
        return response -> parseContentList(serverInstance.loader.type(), response);
    }

    public static List<Content> parseContentList(Loader.Type loader, Response response) {
        verifyRequestType(response, Request.Type.GET_PROJECT_LIST);
        Mimo.LOGGER.debug("Parsing content list for %s".formatted(response));
        JsonArray list = response.data().getAsJsonArray();
        return result(response, parseContentListFromJsonArray(loader, list));
    }

    public static Content parseContent(Loader.Type loader, Response response) {
        verifyRequestType(response, Request.Type.GET_PROJECT);
        Mimo.LOGGER.debug("Parsing content for %s".formatted(response));
        return result(response, parseContent(loader, response.data().getAsJsonObject()));
    }

    public static List<String> parseSearchData(Response response) {
        verifyRequestType(response, Request.Type.SEARCH);
        Mimo.LOGGER.debug("Parsing search data for %s".formatted(response));
        JsonArray hits = response.data().getAsJsonObject().getAsJsonArray("hits");
        return result(response, parseProjectIdListFromJsonArray(hits));
    }

    public static List<Content.Version> parseProjectVersionList(Response response) {
        verifyRequestType(response, Request.Type.GET_PROJECT_VERSION_LIST);
        Mimo.LOGGER.debug("Parsing content version for %s".formatted(response));
        List<Content.Version> versions = new ArrayList<>();
        for (JsonElement element : response.data().getAsJsonArray())
            versions.add(parseVersion(element));

        return result(response, versions);
    }

    public static Content.Version parseVersion(Response response) {
        verifyRequestType(response, Request.Type.LATEST_VERSION, Request.Type.GET_VERSION_FILE);
        Mimo.LOGGER.debug("Parsing version for %s".formatted(response));
        return result(response, parseVersion(response.data()));
    }

    public static Map<String, Content.Version> parseVersionListWithProjectId(Response response) {
        verifyRequestType(response, Request.Type.GET_VERSION_FILE_LIST);
        Mimo.LOGGER.debug("Parsing version list from multiple projects for %s".formatted(response));
        JsonObject data = response.data().getAsJsonObject();
        Map<String, Content.Version> versions = new HashMap<>();
        for (String key : data.keySet()) {
            JsonObject element = data.getAsJsonObject(key);
            versions.put(parseProjectId(element), parseVersion(element));
        }
        return result(response, versions);
    }

    public static String parseProjectId(Response response) {
        verifyRequestType(response, Request.Type.LATEST_VERSION, Request.Type.GET_PROJECT, Request.Type.GET_VERSION_FILE);
        Mimo.LOGGER.debug("Parsing project id for %s".formatted(response));
        return result(response, parseProjectId(response.data()));
    }

    private static ProjectType parseProjectType(JsonObject data) {
        if (!data.has("loaders"))
            return parsed("project type", ProjectType.get(data.get("project_type").getAsString()));
        List<String> loaders = data.get("loaders").getAsJsonArray().asList()
                .stream()
                .map(JsonElement::getAsString)
                .toList();
        if (loaders.contains("datapack") && loaders.size() == 1)
            return parsed("project type", ProjectType.DATAPACK);
        return parsed("project type", ProjectType.MOD);
    }

    private static Content parseContent(Loader.Type loader, JsonObject data) {
        String projectIdKey = data.has("project_id") ? "project_id" : "id";
        String id = data.get(projectIdKey).getAsString();
        String slug = data.get("slug").getAsString();
        ProjectType projectType = parseProjectType(data);
        if (data.has("server_side") && data.get("server_side").getAsString().equals("unsupported"))
            Mimo.LOGGER.warning("Content \"%s\" is client only!!".formatted(slug));

        return parsed("content", new Content(projectType, loader, id, slug));
    }

    private static List<Content> parseContentListFromJsonArray(Loader.Type loader, JsonArray list) {
        List<Content> contents = new ArrayList<>();
        for (JsonElement element : list) {
            JsonObject currentContent = element.getAsJsonObject();
            Content content = parseContent(loader, currentContent);
            contents.add(content);
        }
        return parsed("content list", contents);
    }

    private static List<String> parseProjectIdListFromJsonArray(JsonArray list) {
        List<String> contents = new ArrayList<>();
        for (JsonElement element : list) {
            contents.add(parseSlug(element));
        }
        return parsed("slug list", contents);
    }

    private static Content.Version parseVersion(JsonElement element) {
        JsonObject versionData = element.getAsJsonObject();
        String versionId = versionData.get("id").getAsString();
        JsonArray files = versionData.getAsJsonArray("files");
        if (files.isEmpty())
            throw new IllegalStateException("No available version for " + versionId);
        JsonObject file = files.get(0).getAsJsonObject();
        JsonObject hashes = file.getAsJsonObject("hashes");
        String hash = hashes.get("sha1").getAsString();
        String url = file.get("url").getAsString();
        String fileName = file.get("filename").getAsString();
        JsonArray dependencies = versionData.get("dependencies").getAsJsonArray();
        List<Content.Dependency> versionDependencies = new ArrayList<>();
        for (JsonElement dependency : dependencies) {
            JsonObject dependencyData = dependency.getAsJsonObject();
            String dependencyId = dependencyData.get("project_id").getAsString();
            boolean required = dependencyData.get("dependency_type").getAsString().equals("required");
            versionDependencies.add(new Content.Dependency(dependencyId, required));
        }

        return parsed("version", new Content.Version(versionId, hash, url, fileName, versionDependencies));
    }

    private static String parseProjectId(JsonElement element) {
        JsonObject data = element.getAsJsonObject();
        String projectIdKey = data.has("project_id") ? "project_id" : "id";
        return parsed("project id", data.get(projectIdKey).getAsString());
    }

    private static String parseSlug(JsonElement element) {
        JsonObject data = element.getAsJsonObject();
        return parsed("slug", data.get("slug").getAsString());
    }

    private static <T> T parsed(String name, T value) {
        Mimo.LOGGER.verbose("Parsed %s is \"%s\"".formatted(name, value));
        return value;
    }

    private static <T> T result(Response response, T value) {
        Mimo.LOGGER.debug("Parse Result for %s is \"%s\"".formatted(response, value));
        return value;
    }
}
