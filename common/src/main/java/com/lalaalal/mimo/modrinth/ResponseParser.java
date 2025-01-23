package com.lalaalal.mimo.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ProjectType;

import java.util.*;

public class ResponseParser {
    protected static void verifyRequestType(Response response, Request.Type... requiredTypes) {
        verifyRequestType(response, List.of(requiredTypes));
    }

    protected static void verifyRequestType(Response response, List<Request.Type> requiredTypes) {
        if (!requiredTypes.contains(response.requestType()))
            throw new IllegalArgumentException("Required request type is [%s] but %s".formatted(requiredTypes, response.requestType()));
    }

    protected static Optional<ProjectType> parseProjectType(JsonObject data) {
        if (data.has("project_type"))
            return ProjectType.getOptional(data.get("project_type").getAsString());
        else if (data.has("loaders")) {
            for (JsonElement loader : data.get("loaders").getAsJsonArray().asList()) {
                if (loader.getAsString().equals("minecraft"))
                    return Optional.of(ProjectType.DATAPACK);
            }
            return Optional.of(ProjectType.MOD);
        }
        return Optional.empty();
    }

    protected static Optional<Content> parseContent(JsonObject data) {
        String projectIdKey = data.has("project_id") ? "project_id" : "id";
        String id = data.get(projectIdKey).getAsString();
        String slug = data.get("slug").getAsString();
        Optional<ProjectType> projectType = parseProjectType(data);

        return projectType.map(value -> new Content(value, id, slug));
    }

    public static Content parseContent(Response response) {
        verifyRequestType(response, Request.Type.GET_PROJECT);
        Optional<Content> content = parseContent(response.data().getAsJsonObject());
        return content.orElseThrow();
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

    public static List<Content> parseContentList(Response response) {
        verifyRequestType(response, Request.Type.GET_PROJECT_LIST);
        JsonArray list = response.data().getAsJsonArray();
        return parseContentListFromJsonArray(list);
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

        return new Content.Version(versionId, hash, url, fileName);
    }

    public static List<Content> parseDependencies(Response response) {
        verifyRequestType(response, Request.Type.GET_PROJECT_DEPENDENCY_LIST);
        List<Content> contents = new ArrayList<>();
        JsonArray dependencies = response.data().getAsJsonObject().getAsJsonArray("projects");
        for (JsonElement element : dependencies) {
            JsonObject dependency = element.getAsJsonObject();
            Optional<Content> content = parseContent(dependency);
            content.ifPresent(contents::add);
        }
        return contents;
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
