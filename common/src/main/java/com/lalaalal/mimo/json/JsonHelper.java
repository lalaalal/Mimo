package com.lalaalal.mimo.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lalaalal.mimo.exception.JsonParsingException;

public final class JsonHelper {
    public static void testKeys(JsonObject jsonObject, String... keys) {
        for (String key : keys) {
            if (!jsonObject.has(key))
                throw JsonParsingException.missingKey(jsonObject, key);
        }
    }

    public static JsonObject toJsonObject(JsonElement element) {
        if (element.isJsonObject())
            return element.getAsJsonObject();
        throw JsonParsingException.unexpectedType(element, "JsonObject");
    }

    public static JsonArray toJsonArray(JsonElement element) {
        if (element.isJsonArray())
            return element.getAsJsonArray();
        throw JsonParsingException.unexpectedType(element, "JsonArray");
    }

    public static int toInteger(JsonElement element) {
        try {
            if (element.isJsonPrimitive())
                return element.getAsInt();
        } catch (NumberFormatException exception) {
            throw JsonParsingException.unexpectedType(element, "Integer", exception);
        }
        throw JsonParsingException.unexpectedType(element, "Integer");
    }

    public static float toFloat(JsonElement element) {
        try {
            if (element.isJsonPrimitive())
                return element.getAsFloat();
        } catch (NumberFormatException exception) {
            throw JsonParsingException.unexpectedType(element, "Float", exception);
        }
        throw JsonParsingException.unexpectedType(element, "Float");
    }

    public static String toString(JsonElement element) {
        if (element.isJsonPrimitive())
            return element.getAsString();
        throw JsonParsingException.unexpectedType(element, "String");
    }
}
