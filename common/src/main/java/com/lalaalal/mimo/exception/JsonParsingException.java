package com.lalaalal.mimo.exception;

import com.google.gson.JsonElement;
import com.lalaalal.mimo.logging.MessageComponent;

public class JsonParsingException extends MessageComponentException {
    public static JsonParsingException missingKey(JsonElement element, String missingKey) {
        return new JsonParsingException(
                MessageComponent.withDefault("[JsonParsing]: Missing key \"%s\" in \"%s\"".formatted(missingKey, element))
        );
    }

    public static JsonParsingException unexpectedType(JsonElement element, String expectedType) {
        return new JsonParsingException(
                MessageComponent.withDefault("[JsonParsing]: Failed to convert \"%s\" to %s".formatted(element, expectedType))
        );
    }

    public static JsonParsingException unexpectedType(JsonElement element, String expectedType, Throwable cause) {
        return new JsonParsingException(
                MessageComponent.withDefault("[JsonParsing]: Failed to convert \"%s\" to %s".formatted(element, expectedType)),
                cause
        );
    }

    public static JsonParsingException unexpectedType(JsonElement element, String expectedType, MessageComponentException cause) {
        return new JsonParsingException(
                MessageComponent.withDefault("[JsonParsing]: Failed to convert \"%s\" to %s".formatted(element, expectedType)),
                cause
        );
    }

    public JsonParsingException(MessageComponent component) {
        super(component);
    }

    public JsonParsingException(MessageComponent component, Throwable cause) {
        super(component, cause);
    }

    public JsonParsingException(MessageComponent component, MessageComponentException cause) {
        super(component, cause);
    }
}
