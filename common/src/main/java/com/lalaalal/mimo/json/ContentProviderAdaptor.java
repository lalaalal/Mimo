package com.lalaalal.mimo.json;

import com.google.gson.*;
import com.lalaalal.mimo.Registries;
import com.lalaalal.mimo.content_provider.ContentProvider;

import java.lang.reflect.Type;

public class ContentProviderAdaptor implements JsonSerializer<ContentProvider>, JsonDeserializer<ContentProvider> {
    @Override
    public ContentProvider deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Registries.CONTENT_PROVIDERS.get(jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(ContentProvider contentProvider, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(contentProvider.getName());
    }
}
