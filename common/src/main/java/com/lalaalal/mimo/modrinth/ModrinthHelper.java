package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.data.Content;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

public class ModrinthHelper {
    public static final String API_URL = "https://api.modrinth.com/v2/";

    private static void setupPostRequest(HttpsURLConnection connection, Request request) throws IOException {
        if (!request.isPost())
            return;
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
            writer.write(request.body());
        }
    }

    protected static Response sendRequest(Request request) throws IOException {
        URL url = URL.of(URI.create(API_URL + request.createQuery()), null);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod(request.method());
        connection.setConnectTimeout(2000);
        connection.setUseCaches(false);
        setupPostRequest(connection, request);
        connection.connect();

        int code = connection.getResponseCode();
        InputStream inputStream = connection.getInputStream();
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;

            while ((line = reader.readLine()) != null)
                stringBuilder.append(line);
        }
        String body = stringBuilder.toString();
        return new Response(request.type(), code, body);
    }

    public static void download(Content.Version version, Path path) throws IOException {
        Mimo.download(version.url(), path);
    }

    public static void sendRequest(Request request, Callback callback) {
        try {
            callback.run(sendRequest(request));
        } catch (IOException exception) {
            callback.run(new Response(request.type(), -1, "{\"message\":\"%s\"}".formatted(exception.getMessage())));
        }
    }

    public static Thread createRequestThread(Request request, Callback callback) {
        return new RequestSender(request, callback);
    }

    private static class RequestSender extends Thread {
        private final Request request;
        private final Callback callback;

        private RequestSender(Request request, Callback callback) {
            this.request = request;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                Response response = sendRequest(request);
                callback.run(response);
            } catch (IOException e) {
                callback.run(new Response(request.type(), -1, e.getMessage()));
            }
        }
    }
}
