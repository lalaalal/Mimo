package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.exception.JsonParsingException;
import com.lalaalal.mimo.exception.ResponseParsingException;
import com.lalaalal.mimo.util.HttpHelper;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Function;

public class ModrinthHelper {
    public static final String API_URL = "https://api.modrinth.com/v2/";

    private static void setupPostRequest(HttpsURLConnection connection, Request request) throws IOException {
        if (!request.isPost())
            return;
        Mimo.LOGGER.debug("[REQ %03d] Preparing POST request".formatted(request.id()));
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
            writer.write(request.body());
        }
    }

    protected static Response sendRequest(Request request) throws IOException {
        Mimo.LOGGER.debug("[REQ %03d] Sending request \"%s\"".formatted(request.id(), request.type()));
        URL url = URL.of(URI.create(API_URL + request.createQuery()), null);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod(request.method());
        connection.setConnectTimeout(2000);
        connection.setUseCaches(false);
        setupPostRequest(connection, request);
        Mimo.LOGGER.debug("[REQ %03d] Connecting to \"%s\"".formatted(request.id(), url));
        connection.connect();

        int code = connection.getResponseCode();
        Mimo.LOGGER.debug("[REQ %03d] Response code is %d".formatted(request.id(), code));
        if (code != 200)
            return new Response(request, code, "\"%s\"".formatted(url.toString()));
        Mimo.LOGGER.debug("[REQ %03d] Reading response".formatted(request.id()));
        InputStream inputStream = connection.getInputStream();
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;

            while ((line = reader.readLine()) != null)
                stringBuilder.append(line);
        }
        String body = stringBuilder.toString();
        Mimo.LOGGER.verbose("[REQ %03d] Response body is \"%s\"".formatted(request.id(), body));
        return new Response(request, code, body);
    }

    /**
     * Download the content file from a given version to a given path.
     *
     * @param version Version file to download
     * @param path    Path to save the file to
     * @throws IOException If an I/O error occurs
     */
    public static void download(Content.Version version, Path path) throws IOException {
        HttpHelper.download(version.url(), path);
    }

    /**
     * Send request and return response.
     *
     * @param request Request to send
     * @return Response
     * @see Request
     * @see Response
     */
    public static Response send(Request request) {
        try {
            return sendRequest(request);
        } catch (IOException exception) {
            return new Response(request, -1, "\"%s\"".formatted(exception.getMessage()));
        }
    }

    /**
     * Send request and parse response with given parser.
     *
     * @param request Request to send
     * @param parser  Parser to parse response with
     * @param <T>     Type of the parsed response
     * @return Parsed response
     * @see Request
     * @see ResponseParser
     */
    public static <T> T get(Request request, Function<Response, T> parser) {
        try {
            Response response = send(request);
            if (response.code() == 404)
                throw ResponseParsingException.notFound("[REQ %03d] (%d) Not found %s".formatted(request.id(), response.code(), response.data()));
            if (response.code() != 200)
                throw ResponseParsingException.notFound("[REQ %03d] (%d) Connection failed".formatted(request.id(), response.code()));
            return parser.apply(response);
        } catch (JsonParsingException exception) {
            throw ResponseParsingException.jsonParsing("[REQ %03d] Failed to parse response".formatted(request.id()), exception);
        }
    }
}
