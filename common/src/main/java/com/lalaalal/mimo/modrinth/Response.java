package com.lalaalal.mimo.modrinth;

import com.google.gson.JsonElement;
import com.lalaalal.mimo.Mimo;

public record Response(Request.Type requestType, int code, JsonElement data) {
    public Response(Request.Type requestType, int code, String json) {
        this(requestType, code, Mimo.GSON.fromJson(json, JsonElement.class));
    }
}
