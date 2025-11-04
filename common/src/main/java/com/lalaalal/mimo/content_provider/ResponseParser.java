package com.lalaalal.mimo.content_provider;

import com.lalaalal.mimo.Mimo;

import java.util.List;

public abstract class ResponseParser {
    protected static void verifyRequestType(Response response, Request.Type... requiredTypes) {
        verifyRequestType(response, List.of(requiredTypes));
    }

    protected static void verifyRequestType(Response response, List<Request.Type> requiredTypes) {
        Mimo.LOGGER.debug("[REQ %03d] Verifying request format for {}".formatted(response.id()), response);
        if (!requiredTypes.contains(response.requestType()))
            throw new IllegalArgumentException("[REQ %03d] Required request format is [%s] but %s".formatted(response.id(), requiredTypes, response.requestType()));
    }

    protected static void logStartParsing(String target, Response response) {
        Mimo.LOGGER.debug("[REQ %03d] Starting parsing {} for {}".formatted(response.id()), target, response.requestType());
    }

    protected static <T> T parsed(String name, T value) {
        Mimo.LOGGER.verbose("Parsed {} is \"{}\"", name, value);
        return value;
    }

    protected static <T> T result(Response response, T value) {
        Mimo.LOGGER.debug("[REQ %03d] Parse result for {} is \"{}\"".formatted(response.id()), response.requestType(), value);
        return value;
    }
}
