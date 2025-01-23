package com.lalaalal.mimo.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ProjectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResponseHandler {
    protected static void verifyRequestType(Request.Type required, Response response) {
        if (response.requestType() != required)
            throw new IllegalArgumentException("Required request type is %s but %s".formatted(required, response.requestType()));
    }

    protected static Optional<Content> parseContent(JsonObject data) {
        String type = data.get("project_type").getAsString();
        String projectIdKey = data.has("project_id") ? "project_id" : "id";
        String id = data.get(projectIdKey).getAsString();
        String slug = data.get("slug").getAsString();
        Optional<ProjectType> projectType = ProjectType.getOptional(type);
        return projectType.map(value -> new Content(value, id, slug));
    }

    public static List<Content> parseSearchData(Response response) {
        verifyRequestType(Request.Type.SEARCH, response);
        List<Content> contents = new ArrayList<>();
        JsonArray hits = response.data().getAsJsonObject().getAsJsonArray("hits");
        for (JsonElement element : hits) {
            JsonObject currentContent = element.getAsJsonObject();
            Optional<Content> content = parseContent(currentContent);
            content.ifPresent(contents::add);
        }
        return contents;
    }

    public static List<Content.Version> parseVersionDataList(Response response) {
        verifyRequestType(Request.Type.PROJECT_VERSION, response);
        List<Content.Version> versions = new ArrayList<>();
        for (JsonElement element : response.data().getAsJsonArray())
            versions.add(parseLatestVersionData(element));

        return versions;
    }

    protected static Content.Version parseLatestVersionData(JsonElement element) {
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
        verifyRequestType(Request.Type.PROJECT_DEPENDENCIES, response);
        List<Content> contents = new ArrayList<>();
        JsonArray dependencies = response.data().getAsJsonObject().getAsJsonArray("projects");
        for (JsonElement element : dependencies) {
            JsonObject dependency = element.getAsJsonObject();
            Optional<Content> content = parseContent(dependency);
            content.ifPresent(contents::add);
        }
        return contents;
    }

    public static Content.Version parseLatestVersionData(Response response) {
        verifyRequestType(Request.Type.LATEST_VERSION, response);
        return parseLatestVersionData(response.data());
    }
}
