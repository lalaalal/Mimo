package com.lalaalal.mimo.curseforge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lalaalal.mimo.content_provider.Request;
import com.lalaalal.mimo.content_provider.Response;
import com.lalaalal.mimo.content_provider.ResponseParser;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.data.ProjectType;
import com.lalaalal.mimo.json.JsonHelper;
import com.lalaalal.mimo.loader.Loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CurseForgeResponseParser extends ResponseParser {
    private static final Map<Loader.Type, String> MOD_LOADER_TYPE_MAP = Map.of(
            Loader.Type.FABRIC, "Fabric",
            Loader.Type.NEOFORGE, "NeoForge",
            Loader.Type.DATAPACK, "Datapack"
    );

    private static JsonObject dataObject(Response response) {
        JsonObject rootObject = JsonHelper.toJsonObject(response.data());
        JsonHelper.testKeys(rootObject, "data");
        return JsonHelper.toJsonObject(rootObject.get("data"));
    }

    private static JsonArray dataArray(Response response) {
        JsonObject rootObject = JsonHelper.toJsonObject(response.data());
        JsonHelper.testKeys(rootObject, "data");
        return JsonHelper.toJsonArray(rootObject.get("data"));
    }

    public List<Content> parseContents(MinecraftVersion version, Loader.Type type, Response response) {
        verifyRequestType(response, Request.Type.SEARCH, Request.Type.GET_SINGLE_PROJECT);
        logStartParsing("content", response);
        return result(response, parseContents(version, type, dataArray(response)));
    }

    public Content parseContent(MinecraftVersion version, Loader.Type type, Response response) {
        verifyRequestType(response, Request.Type.SEARCH, Request.Type.GET_SINGLE_PROJECT);
        logStartParsing("content", response);
        return result(response, parseContentOrThrow(version, type, dataObject(response)));
    }

    public List<Content.Version> parseProjectVersionList(MinecraftVersion version, Loader.Type type, Response response) {
        verifyRequestType(response, Request.Type.GET_SINGLE_VERSION_LIST);
        logStartParsing("content version list", response);
        return result(response, parseVersions(version, type, dataArray(response)));
    }

    private Content parseContentOrThrow(MinecraftVersion version, Loader.Type type, JsonObject dataObject) {
        return parseContent(version, type, dataObject).orElseThrow(() -> new RuntimeException("Cannot find content for " + type + " [" + version + "] "));
    }

    private Optional<Content> parseContent(MinecraftVersion version, Loader.Type type, JsonObject dataObject) {
        JsonHelper.testKeys(dataObject, "id", "slug");
        String id = JsonHelper.toString(dataObject.get("id"));
        String slug = JsonHelper.toString(dataObject.get("slug"));
        if (verifyLoader(version, type, dataObject))
            return Optional.of(parsed("content", new Content(ProjectType.MOD, type, CurseForgeContentProvider.INSTANCE, id, slug)));
        return Optional.empty();
    }

    private List<Content> parseContents(MinecraftVersion version, Loader.Type type, JsonArray dataArray) {
        List<Content> contents = new ArrayList<>();
        for (JsonElement data : dataArray) {
            JsonObject dataObject = JsonHelper.toJsonObject(data);
            parseContent(version, type, dataObject).ifPresent(contents::add);
        }
        return contents;
    }

    private List<Content.Version> parseVersions(MinecraftVersion version, Loader.Type type, JsonArray dataArray) {
        List<Content.Version> contents = new ArrayList<>();
        for (JsonElement latestFile : dataArray) {
            if (testVersion(version, type, latestFile)) {
                JsonObject latestFileObject = JsonHelper.toJsonObject(latestFile);
                JsonHelper.testKeys(latestFileObject, "id", "hashes", "downloadUrl", "fileName", "dependencies");
                String id = JsonHelper.toString(latestFileObject.get("id"));
                String url = JsonHelper.toString(latestFileObject.get("downloadUrl"));
                String fileName = JsonHelper.toString(latestFileObject.get("fileName"));
                String hash = parseHash(JsonHelper.toJsonArray(latestFileObject.get("hashes")));
                List<Content.Dependency> dependencies = parseDependencies(latestFileObject.get("dependencies"));
                contents.add(new Content.Version(id, hash, url, fileName, dependencies));
            }
        }
        contents.sort((v1, v2) -> v2.fileName().compareTo(v1.fileName()));
        return contents;
    }

    private String parseHash(JsonArray hashArray) {
        for (JsonElement hash : hashArray) {
            JsonObject hashObject = JsonHelper.toJsonObject(hash);
            JsonHelper.testKeys(hashObject, "algo", "value");
            int algo = JsonHelper.toInteger(hashObject.get("algo"));
            if (algo == 1)
                return JsonHelper.toString(hashObject.get("value"));
        }
        return "";
    }

    private List<Content.Dependency> parseDependencies(JsonElement dependencies) {
        JsonArray dependencyArray = JsonHelper.toJsonArray(dependencies);
        List<Content.Dependency> result = new ArrayList<>();
        for (JsonElement dependency : dependencyArray) {
            JsonObject dependencyObject = JsonHelper.toJsonObject(dependency);
            JsonHelper.testKeys(dependencyObject, "modId", "relationType");
            String modId = JsonHelper.toString(dependencyObject.get("modId"));
            int relationType = JsonHelper.toInteger(dependencyObject.get("relationType"));
            result.add(new Content.Dependency(modId, relationType == 3));
        }
        return result;
    }

    private boolean verifyLoader(MinecraftVersion version, Loader.Type type, JsonObject dataObject) {
        JsonHelper.testKeys(dataObject, "latestFiles");
        JsonArray latestFiles = JsonHelper.toJsonArray(dataObject.get("latestFiles"));
        for (JsonElement latestFile : latestFiles) {
            if (testVersion(version, type, latestFile))
                return true;
        }
        return false;
    }

    private boolean testVersion(MinecraftVersion version, Loader.Type type, JsonElement latestFile) {
        JsonObject latestFileObject = JsonHelper.toJsonObject(latestFile);
        JsonHelper.testKeys(latestFileObject, "gameVersions");
        JsonArray gameVersions = JsonHelper.toJsonArray(latestFileObject.get("gameVersions"));
        boolean versionFound = false;
        boolean loaderFound = false;
        for (JsonElement gameVersion : gameVersions) {
            String gameVersionString = gameVersion.getAsString();
            if (gameVersionString.equals(MOD_LOADER_TYPE_MAP.get(type)))
                loaderFound = true;
            if (gameVersionString.equals(version.name()))
                versionFound = true;
        }
        return loaderFound && versionFound;
    }
}
