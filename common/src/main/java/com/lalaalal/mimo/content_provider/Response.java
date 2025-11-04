package com.lalaalal.mimo.content_provider;

import com.google.gson.JsonElement;
import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.modrinth.ModrinthResponseParser;

/**
 * Response from Modrinth API.
 *
 * @param id          Request id
 * @param requestType Request format
 * @param code        HTTP response code
 * @param data        Response data
 * @see ModrinthResponseParser
 */
public record Response(int id, Request.Type requestType, int code, JsonElement data) {
    public Response(Request request, int code, String json) {
        this(request.id(), request.format().type(), code, Mimo.GSON.fromJson(json, JsonElement.class));
    }

    @Override
    public String toString() {
        return "[" + id + ", " + requestType + ", " + code + "]";
    }
}
