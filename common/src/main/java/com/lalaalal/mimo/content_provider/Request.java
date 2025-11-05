package com.lalaalal.mimo.content_provider;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Request for Modrinth API.
 *
 * @param id     Request id
 * @param format {@link Format} of the request
 * @param params Parameters for the request
 * @param body   Body of the http request
 */
public record Request(int id, Format format, Map<String, String> params, String body) {
    private static int counter = 0;

    private static int nextId() {
        return counter++;
    }

    /**
     * Create a request with auto-incremented id.
     *
     * @param format   {@link Format} of the request
     * @param params Parameters for the request
     * @param body   Body of the http request
     */
    public Request(Format format, Map<String, String> params, String body) {
        this(nextId(), format, params, body);
    }

    /**
     * Create a request with an empty body and auto-incremented id.
     *
     * @param format   {@link Format} of the request
     * @param params Parameters for the request
     */
    public Request(Format format, Map<String, String> params) {
        this(format, params, "");
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
        return format.makeQuery(params);
    }

    @Override
    public String toString() {
        return "[" + id + ", " + format + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Request request)) return false;
        return body.equals(request.body) && format.equals(request.format) && params.equals(request.params);
    }

    @Override
    public int hashCode() {
        int result = format.hashCode();
        result = 31 * result + params.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }

    public enum Type {
        EMPTY(true),
        SEARCH(true),
        GET_PROJECT(true),
        GET_PROJECT_LIST(true),
        @Deprecated
        GET_PROJECT_DEPENDENCY_LIST(false),
        GET_PROJECT_VERSION_LIST(false),
        LATEST_VERSION(false),
        GET_VERSION_FILE(true),
        GET_VERSION_FILE_LIST(true);

        private final boolean canBeReused;

        Type(boolean canBeReused) {
            this.canBeReused = canBeReused;
        }

        public boolean canBeReused() {
            return canBeReused;
        }
    }

    public record Format(Type type, QueryMaker queryMaker, String queryFormat) {
        private String makeQuery(Map<String, String> params) {
            return queryMaker.makeQuery(queryFormat, params);
        }

        @Override
        public String toString() {
            return type.name();
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
         * Key of params should be like ${key}.
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
