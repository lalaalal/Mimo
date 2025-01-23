package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ContentFilter;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.loader.Loader;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Request(Type type, Map<String, String> params, String body) {
    public static Request search(String name, ContentFilter filter) {
        return new Request(Type.SEARCH,
                Map.of(
                        "query", name,
                        "facets", filter.toString()
                )
        );
    }

    public static Request get(String slug) {
        return new Request(Type.GET_PROJECT,
                Map.of("${id}", slug)
        );
    }

    public static Request dependencies(String slug) {
        return new Request(Type.PROJECT_DEPENDENCIES,
                Map.of("${id}", slug)
        );
    }

    public static Request versions(String slug, MinecraftVersion version, Loader.Type loader) {
        return new Request(Type.PROJECT_VERSION,
                Map.of(
                        "${id}", slug,
                        "loaders", "[\"%s\"]".formatted(loader),
                        "game_versions", "[\"%s\"]".formatted(version)
                )
        );
    }

    public static Request versions(String slug, MinecraftVersion version, Loader loader) {
        return versions(slug, version, loader.type());
    }

    public static Request versions(String slug, ServerInstance instance) {
        return versions(slug, instance.version, instance.loader);
    }

    public static Request latestVersion(Content.Version version, ServerInstance instance) {
        String body = Mimo.GSON.toJson(Map.of(
                "loaders", new String[]{instance.loader.type().toString()},
                "game_versions", new String[]{instance.version.toString()}
        ));
        return new Request(Type.LATEST_VERSION,
                Map.of("${hash}", version.hash()),
                body
        );
    }

    public Request(Type type, Map<String, String> params) {
        this(type, params, "");
    }

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

    public enum Type {
        SEARCH(QueryMaker.QUERY_PARAM),
        GET_PROJECT(QueryMaker.PATH_PARAM, "project/${id}"),
        PROJECT_DEPENDENCIES(QueryMaker.PATH_PARAM, "project/${id}/dependencies"),
        PROJECT_VERSION(QueryMaker.MIXED, "project/${id}/version"),
        LATEST_VERSION(QueryMaker.PATH_PARAM, "version_file/${hash}/update");

        private final QueryMaker queryMaker;
        private final String queryFormat;

        Type(QueryMaker queryMaker) {
            this.queryMaker = queryMaker;
            this.queryFormat = name().toLowerCase();
        }

        Type(QueryMaker queryMaker, String format) {
            this.queryMaker = queryMaker;
            this.queryFormat = format;
        }

        private String makeQuery(Map<String, String> params) {
            return queryMaker.makeQuery(queryFormat, params);
        }
    }

    protected interface QueryMaker {
        QueryMaker PATH_PARAM = (format, params) -> {
            Pattern pattern = Pattern.compile("\\$\\{[a-z]+}");
            Matcher matcher = pattern.matcher(format);
            while (matcher.find()) {
                String key = matcher.group();
                format = format.replace(key, URLEncoder.encode(params.get(key), StandardCharsets.UTF_8));
            }

            return format;
        };
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
        QueryMaker MIXED = (format, params) -> {
            String path = PATH_PARAM.makeQuery(format, params);
            return QUERY_PARAM.makeQuery(path, params);
        };

        String makeQuery(String format, Map<String, String> params);
    }
}
