package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ContentFilter;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.loader.Loader;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Request for Modrinth API.
 *
 * @param id     Request id
 * @param type   {@link Type} of the request
 * @param params Parameters for the request
 * @param body   Body of the http request
 */
public record Request(int id, Type type, Map<String, String> params, String body) {
    private static int counter = 0;

    private static int nextId() {
        return counter++;
    }

    public static Request search(String name, ContentFilter filter) {
        return new Request(Type.SEARCH,
                Map.of(
                        "query", name,
                        "facets", filter.toString()
                )
        );
    }

    public static Request project(String id) {
        return new Request(Type.GET_PROJECT,
                Map.of("${id}", id)
        );
    }

    public static Request projects(Collection<String> ids) {
        return new Request(Type.GET_PROJECT_LIST,
                Map.of("ids", Mimo.GSON.toJson(ids))
        );
    }

    public static Request projects(String... ids) {
        return projects(List.of(ids));
    }

    public static Request version(String hash) {
        return new Request(Type.GET_VERSION_FILE,
                Map.of("${hash}", hash)
        );
    }

    public static Request versions(Collection<String> hashes) {
        String body = Mimo.GSON.toJson(
                Map.of(
                        "hashes", hashes,
                        "algorithm", "sha1"
                )
        );
        return new Request(Type.GET_VERSION_FILE_LIST, Map.of(), body);
    }

    public static Request versions(String... hashes) {
        return versions(List.of(hashes));
    }

    public static Request dependencies(String slug) {
        return new Request(Type.GET_PROJECT_DEPENDENCY_LIST,
                Map.of("${id}", slug)
        );
    }

    public static Request projectVersions(String slug, MinecraftVersion version, Loader.Type loader) {
        return new Request(Type.GET_PROJECT_VERSION_LIST,
                Map.of(
                        "${id}", slug,
                        "loaders", "[\"%s\"]".formatted(loader),
                        "game_versions", "[\"%s\"]".formatted(version)
                )
        );
    }

    public static Request projectVersions(Content content, ServerInstance instance) {
        return projectVersions(content.slug(), instance.version, content.loader());
    }

    public static Request latestVersion(Content content, Content.Version version, ServerInstance instance) {
        String body = Mimo.GSON.toJson(Map.of(
                "loaders", new String[]{content.loader().toString()},
                "game_versions", new String[]{instance.version.toString()}
        ));
        return new Request(Type.LATEST_VERSION,
                Map.of("${hash}", version.hash()),
                body
        );
    }

    /**
     * Create a request with auto-incremented id.
     *
     * @param type   {@link Type} of the request
     * @param params Parameters for the request
     * @param body   Body of the http request
     */
    public Request(Type type, Map<String, String> params, String body) {
        this(nextId(), type, params, body);
    }

    /**
     * Create a request with an empty body and auto-incremented id.
     *
     * @param type   {@link Type} of the request
     * @param params Parameters for the request
     */
    public Request(Type type, Map<String, String> params) {
        this(type, params, "");
    }

    /**
     * Http method for the request.
     *
     * @return "GET" or "POST"
     */
    public String method() {
        if (body.isEmpty())
            return "GET";
        return "POST";
    }

    public boolean isPost() {
        return method().equals("POST");
    }

    public String createQuery() {
        return type.makeQuery(params);
    }

    @Override
    public String toString() {
        return "[" + id + ", " + type + "]";
    }

    public enum Type {
        SEARCH(QueryMaker.QUERY_PARAM, "search"),
        GET_PROJECT(QueryMaker.PATH_PARAM, "project/${id}"),
        GET_PROJECT_LIST(QueryMaker.QUERY_PARAM, "projects"),
        GET_PROJECT_DEPENDENCY_LIST(QueryMaker.PATH_PARAM, "project/${id}/dependencies"),
        GET_PROJECT_VERSION_LIST(QueryMaker.MIXED, "project/${id}/version"),
        GET_VERSION_FILE(QueryMaker.PATH_PARAM, "version_file/${hash}"),
        GET_VERSION_FILE_LIST(QueryMaker.EXACT, "version_files"),
        LATEST_VERSION(QueryMaker.PATH_PARAM, "version_file/${hash}/update");

        private final QueryMaker queryMaker;
        private final String queryFormat;

        Type(QueryMaker queryMaker, String format) {
            this.queryMaker = queryMaker;
            this.queryFormat = format;
        }

        private String makeQuery(Map<String, String> params) {
            return queryMaker.makeQuery(queryFormat, params);
        }
    }

    @FunctionalInterface
    public interface QueryMaker {
        QueryMaker EXACT = (format, params) -> format;

        /**
         * Create a query with path parameters.
         * <b>${name}</b> in the format string will be replaced with the value of the name parameter.
         * Key of params should also be the same.
         */
        QueryMaker PATH_PARAM = (format, params) -> {
            Pattern pattern = Pattern.compile("\\$\\{[a-z]+}");
            Matcher matcher = pattern.matcher(format);
            while (matcher.find()) {
                String key = matcher.group();
                format = format.replace(key, URLEncoder.encode(params.get(key), StandardCharsets.UTF_8));
            }

            return format;
        };

        /**
         * Create a query with query parameters.
         * Parameters will be added to the end of the query string.
         * Key of params should only contain characters.
         */
        QueryMaker QUERY_PARAM = (format, params) -> {
            StringBuilder builder = new StringBuilder(format + '?');
            for (String key : params.keySet()) {
                if (!key.startsWith("$"))
                    builder.append(key)
                            .append('=')
                            .append(URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
                            .append('&');
            }
            builder.deleteCharAt(builder.length() - 1);
            return builder.toString();
        };

        /**
         * Create a query with both path and query parameters.
         */
        QueryMaker MIXED = (format, params) -> {
            String path = PATH_PARAM.makeQuery(format, params);
            return QUERY_PARAM.makeQuery(path, params);
        };

        String makeQuery(String format, Map<String, String> params);
    }
}
