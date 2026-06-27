package com.lalaalal.mimo.json;

import com.google.gson.*;
import com.lalaalal.mimo.registry.Registries;
import com.lalaalal.mimo.registry.RegistryItem;
import com.lalaalal.mimo.registry.RegistryKey;

import java.lang.reflect.Type;

public class RegistryItemAdaptor implements JsonSerializer<RegistryItem<?>>, JsonDeserializer<RegistryItem<?>> {
    @Override
    public RegistryItem<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RegistryKey key = context.deserialize(json, RegistryKey.class);
        return Registries.ROOT.getOrThrow(key.registry()).get(key);
    }

    @Override
    public JsonElement serialize(RegistryItem<?> src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.key());
    }
}
