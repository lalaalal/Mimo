package com.lalaalal.mimo.exception;

public class ResponseParsingException extends MessageComponentException {
    public final Type type;

    public static ResponseParsingException notFound(String message) {
        return new ResponseParsingException(Type.NOT_FOUND, message);
    }

    public static ResponseParsingException jsonParsing(String message, MessageComponentException cause) {
        return new ResponseParsingException(Type.JSON_PARSING, message, cause);
    }

    public ResponseParsingException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public ResponseParsingException(Type type, String message, MessageComponentException cause) {
        super(message, cause);
        this.type = type;
    }

    public enum Type {
        JSON_PARSING,
        NOT_FOUND
    }
}
