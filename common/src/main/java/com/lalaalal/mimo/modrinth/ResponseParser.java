package com.lalaalal.mimo.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ProjectType;
import com.lalaalal.mimo.loader.Loader;

import java.util.*;
import java.util.function.Function;

public class ResponseParser {
    protected static void verifyRequestType(Response response, Request.Type... requiredTypes) {
        verifyRequestType(response, List.of(requiredTypes));
    }

    protected static void verifyRequestType(Response response, List<Request.Type> requiredTypes) {
        if (!requiredTypes.contains(response.requestType()))
            throw new IllegalArgumentException("Required request loader is [%s] but %s".formatted(requiredTypes, response.requestType()));
    }

    protected static ProjectType parseProjectType(JsonObject data) {
        if (!data.has("loaders"))
            return ProjectType.get(data.get("project_type").getAsString());
        List<String> loaders = data.get("loaders").getAsJsonArray().asList()
                .stream()
                .map(JsonElement::getAsString)
                .toList();
        if (loaders.contains("datapack") && loaders.size() == 1)
            return ProjectType.DATAPACK;
        return ProjectType.MOD;
    }

    protected static List<Loader.Type> parseLoaders(JsonObject data) {
        if (!data.has("loaders"))
            return List.of();
        List<JsonElement> loaders = data.get("loaders").getAsJsonArray().asList();
        return loaders.stream().map(JsonElement::getAsString)
                .map(Loader.Type::get)
                .filter(Objects::nonNull)
                .toList();
    }

    protected static Optional<Content> parseContent(Loader.Type loader, JsonObject data) {
        String projectIdKey = data.has("project_id") ? "project_id" : "id";
        String id = data.get(projectIdKey).getAsString();
        String slug = data.get("slug").getAsString();
        ProjectType projectType = parseProjectType(data);

        return Optional.of(new Content(projectType, loader, id, slug));
    }

    protected static Optional<Content> parseContent(JsonObject data) {
        String projectIdKey = data.has("project_id") ? "project_id" : "id";
        String id = data.get(projectIdKey).getAsString();
        String slug = data.get("slug").getAsString();
        ProjectType projectType = parseProjectType(data);
        List<Loader.Type> loaders = parseLoaders(data);

        return Optional.of(new Content(projectType, loaders, id, slug));
    }

    public static Content parseContent(Loader.Type loader, Response response) {
        verifyRequestType(response, Request.Type.GET_PROJECT);
        Optional<Content> content = parseContent(loader, response.data().getAsJsonObject());
        return content.orElseThrow();
    }

    public static Function<Response, Content> contentParser(ServerInstance serverInstance) {
        return response -> parseContent(serverInstance.loader.type(), response);
    }

    protected static List<Content> parseContentListFromJsonArray(Loader.Type loader, JsonArray list) {
        List<Content> contents = new ArrayList<>();
        for (JsonElement element : list) {
            JsonObject currentContent = element.getAsJsonObject();
            Optional<Content> content = parseContent(loader, currentContent);
            content.ifPresent(contents::add);
        }
        return contents;
    }

    protected static List<Content> parseContentListFromJsonArray(JsonArray list) {
        List<Content> contents = new ArrayList<>();
        for (JsonElement element : list) {
            JsonObject currentContent = element.getAsJsonObject();
            Optional<Content> content = parseContent(currentContent);
            content.ifPresent(contents::add);
        }
        return contents;
    }


    public static List<Content> parseContentList(Loader.Type loader, Response response) {
        verifyRequestType(response, Request.Type.GET_PROJECT_LIST);
        JsonArray list = response.data().getAsJsonArray();
        return parseContentListFromJsonArray(loader, list);
    }

    public static Function<Response, List<Content>> contentListParser(ServerInstance serverInstance) {
        return response -> parseContentList(serverInstance.loader.type(), response);
    }

    public static List<Content> parseSearchData(Response response) {
        verifyRequestType(response, Request.Type.SEARCH);
        JsonArray hits = response.data().getAsJsonObject().getAsJsonArray("hits");
        return parseContentListFromJsonArray(hits);
    }

    public static List<Content.Version> parseProjectVersionList(Response response) {
        verifyRequestType(response, Request.Type.GET_PROJECT_VERSION_LIST);
        List<Content.Version> versions = new ArrayList<>();
        for (JsonElement element : response.data().getAsJsonArray())
            versions.add(parseVersion(element));

        return versions;
    }

    protected static Content.Version parseVersion(JsonElement element) {
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

        return new Content.Version(versionId, hash, url, fileName, versionDependencies);
    }

    public static Content.Version parseVersion(Response response) {
        verifyRequestType(response, Request.Type.LATEST_VERSION, Request.Type.GET_VERSION_FILE);
        return parseVersion(response.data());
    }

    public static Map<String, Content.Version> parseVersionListWithProjectId(Response response) {
        verifyRequestType(response, Request.Type.GET_VERSION_FILE_LIST);
        JsonObject data = response.data().getAsJsonObject();
        Map<String, Content.Version> versions = new HashMap<>();
        for (String key : data.keySet()) {
            JsonObject element = data.getAsJsonObject(key);
            versions.put(parseProjectId(element), parseVersion(element));
        }
        return versions;
    }

    public static String parseProjectId(Response response) {
        verifyRequestType(response, Request.Type.LATEST_VERSION, Request.Type.GET_PROJECT, Request.Type.GET_VERSION_FILE);
        return parseProjectId(response.data());
    }

    protected static String parseProjectId(JsonElement element) {
        JsonObject data = element.getAsJsonObject();
        String projectIdKey = data.has("project_id") ? "project_id" : "id";
        return data.get(projectIdKey).getAsString();
    }
}
