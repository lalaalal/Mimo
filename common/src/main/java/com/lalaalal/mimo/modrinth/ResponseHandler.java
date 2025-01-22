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
    public static List<Content> resolveSearchData(Response response) {
        List<Content> contents = new ArrayList<>();
        JsonArray hits = response.data().getAsJsonObject().getAsJsonArray("hits");
        for (JsonElement element : hits) {
            JsonObject currentContent = element.getAsJsonObject();
            String type = currentContent.get("project_type").getAsString();
            String slug = currentContent.get("slug").getAsString();
            Optional<ProjectType> optional = ProjectType.getOptional(type);
            optional.ifPresent(projectType -> contents.add(new Content(projectType, slug)));
        }
        return contents;
    }

    public static List<Content.Version> resolveVersionDataList(Response response) {
        List<Content.Version> versions = new ArrayList<>();
        for (JsonElement element : response.data().getAsJsonArray())
            versions.add(resolveVersionData(element));

        return versions;
    }

    protected static Content.Version resolveVersionData(JsonElement element) {
        JsonObject versionData = element.getAsJsonObject();
        String versionId = versionData.get("id").getAsString();
        JsonArray files = versionData.get("files").getAsJsonArray();
        if (files.isEmpty())
            throw new IllegalStateException("No available version for " + versionId);
        JsonObject file = files.get(0).getAsJsonObject();
        JsonObject hashes = file.get("hashes").getAsJsonObject();
        String hash = hashes.get("sha1").getAsString();
        String url = file.get("url").getAsString();
        String fileName = file.get("filename").getAsString();
        return new Content.Version(versionId, hash, url, fileName);
    }

    public static Content.Version resolveVersionData(Response response) {
        return resolveVersionData(response.data());
    }
}
