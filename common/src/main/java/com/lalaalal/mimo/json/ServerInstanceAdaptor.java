package com.lalaalal.mimo.json;

import com.google.gson.*;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.loader.Loader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ServerInstanceAdaptor implements JsonSerializer<ServerInstance>, JsonDeserializer<ServerInstance> {
    @Override
    public JsonElement serialize(ServerInstance src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject data = new JsonObject();
        data.addProperty("name", src.name);
        data.add("loader", context.serialize(src.loader));
        data.add("version", context.serialize(src.version));
        data.addProperty("path", src.path.toString());
        data.add("contents", context.serialize(src.getContents()));
        return data;
    }

    @Override
    public ServerInstance deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject data = json.getAsJsonObject();
        String name = data.get("name").getAsString();
        Loader loader = context.deserialize(data.get("loader"), Loader.class);
        MinecraftVersion minecraftVersion = context.deserialize(data.get("version"), MinecraftVersion.class);
        Path path = Path.of(data.get("path").getAsString());
        JsonArray contents = data.getAsJsonArray("contents");
        Map<Content, Content.Version> contentVersions = new HashMap<>();
        for (JsonElement element : contents) {
            JsonObject contentObject = element.getAsJsonObject();
            Content content = context.deserialize(contentObject.get("content"), Content.class);
            Content.Version contentVersion = context.deserialize(contentObject.get("versions"), Content.Version.class);
            contentVersions.put(content, contentVersion);
        }
        try {
            ServerInstance serverInstance = new ServerInstance(name, loader, minecraftVersion, path);
            serverInstance.setContents(contentVersions);
            return serverInstance;
        } catch (IOException exception) {
            throw new RuntimeException("Failed to create server instance", exception);
        }
    }
}
