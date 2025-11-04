package com.lalaalal.mimo.curseforge;

import com.lalaalal.mimo.content_provider.Request;

import java.util.Map;

public class CurseForgeRequestFactory {
    private static final String MINECRAFT_GAME_ID = "432";

    private static final Request.Format GET_GAMES = new Request.Format(Request.Type.EMPTY, Request.QueryMaker.EXACT, "/games");
    private static final Request.Format SEARCH = new Request.Format(Request.Type.SEARCH, Request.QueryMaker.QUERY_PARAM, "/mods/search");
    private static final Request.Format GET_MOD = new Request.Format(Request.Type.GET_PROJECT, Request.QueryMaker.PATH_PARAM, "/mods/${id}");
    private static final Request.Format GET_FILES = new Request.Format(Request.Type.GET_VERSION_FILE_LIST, Request.QueryMaker.PATH_PARAM, "/mods/${id}/files");
    private static final Request.Format GET_GAME_VERSION_TYPES = new Request.Format(Request.Type.EMPTY, Request.QueryMaker.EXACT, "/games/432/version-types");

    public Request games() {
        return new Request(GET_GAMES, Map.of());
    }

    public Request gameVersionTypes() {
        return new Request(GET_GAME_VERSION_TYPES, Map.of());
    }

    public Request searchSlug(String slug) {
        return new Request(SEARCH, Map.of("gameId", MINECRAFT_GAME_ID, "slug", slug));
    }

    public Request search(String text) {
        return new Request(SEARCH, Map.of("gameId", MINECRAFT_GAME_ID, "searchFilter", text, "sortField", "4"));
    }

    public Request mod(String id) {
        return new Request(GET_MOD, Map.of("${id}", id));
    }

    public Request files(String id) {
        return new Request(GET_FILES, Map.of("${id}", id));
    }
}
