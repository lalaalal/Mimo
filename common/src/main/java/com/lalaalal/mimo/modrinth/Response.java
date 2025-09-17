package com.lalaalal.mimo.modrinth;

import com.google.gson.JsonElement;
import com.lalaalal.mimo.Mimo;

/**
 * Response from Modrinth API.
 *
 * @param id          Request id
 * @param requestType Request type
 * @param code        HTTP response code
 * @param data        Response data
 * @see ResponseParser
 */
public record Response(int id, Request.Type requestType, int code, JsonElement data) {
    public Response(Request request, int code, String json) {
        this(request.id(), request.type(), code, Mimo.GSON.fromJson(json, JsonElement.class));
    }

    @Override
    public String toString() {
        return "[" + id + ", " + requestType + ", " + code + "]";
    }
}
