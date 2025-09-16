package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.data.Content;
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
        Mimo.LOGGER.debug("Preparing POST request");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
            writer.write(request.body());
        }
    }

    protected static Response sendRequest(Request request) throws IOException {
        Mimo.LOGGER.debug("Sending request \"%s\"".formatted(request));
        URL url = URL.of(URI.create(API_URL + request.createQuery()), null);
        Mimo.LOGGER.debug("Connecting to \"%s\"".formatted(url));
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod(request.method());
        connection.setConnectTimeout(2000);
        connection.setUseCaches(false);
        setupPostRequest(connection, request);
        connection.connect();

        int code = connection.getResponseCode();
        Mimo.LOGGER.debug("Response code is %d".formatted(code));
        Mimo.LOGGER.debug("Reading response");
        InputStream inputStream = connection.getInputStream();
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;

            while ((line = reader.readLine()) != null)
                stringBuilder.append(line);
        }
        String body = stringBuilder.toString();
        Mimo.LOGGER.debug("Response body is \"%s\"".formatted(body));
        return new Response(request, code, body);
    }

    public static void download(Content.Version version, Path path) throws IOException {
        HttpHelper.download(version.url(), path);
    }

    public static Response send(Request request) {
        try {
            return sendRequest(request);
        } catch (IOException exception) {
            return new Response(request, -1, "{\"message\":\"%s\"}".formatted(exception.getMessage()));
        }
    }

    public static <T> T get(Request request, Function<Response, T> parser) {
        Response response = send(request);
        return parser.apply(response);
    }
}
