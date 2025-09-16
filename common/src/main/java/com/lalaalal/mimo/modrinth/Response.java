package com.lalaalal.mimo.modrinth;

import com.google.gson.JsonElement;
import com.lalaalal.mimo.Mimo;

public record Response(int id, Request.Type requestType, int code, JsonElement data) {
    public Response(Request request, int code, String json) {
        this(request.id(), request.type(), code, Mimo.GSON.fromJson(json, JsonElement.class));
    }

    @Override
    public String toString() {
        return "[" + id + ", " + requestType + ", " + code + "]";
    }
}
