package com.lalaalal.mimo.modrinth;

import com.google.gson.JsonElement;
import com.lalaalal.mimo.Mimo;

public record Response(int code, JsonElement data) {
    public Response(int code, String json) {
        this(code, Mimo.GSON.fromJson(json, JsonElement.class));
    }
}
