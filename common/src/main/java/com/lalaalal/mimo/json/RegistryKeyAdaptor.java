package com.lalaalal.mimo.json;

import com.google.gson.*;
import com.lalaalal.mimo.registry.RegistryKey;

import java.lang.reflect.Type;

public class RegistryKeyAdaptor implements JsonDeserializer<RegistryKey>, JsonSerializer<RegistryKey> {
    @Override
    public RegistryKey deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return RegistryKey.of(json.getAsString());
    }

    @Override
    public JsonElement serialize(RegistryKey src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
