package com.lalaalal.mimo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lalaalal.mimo.json.MimoExcludeStrategy;
import com.lalaalal.mimo.json.ServerInstanceAdaptor;
import com.lalaalal.mimo.loader.LoaderInstaller;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Mimo {
    public static final Gson GSON = new GsonBuilder()
            .addSerializationExclusionStrategy(new MimoExcludeStrategy())
            .addDeserializationExclusionStrategy(new MimoExcludeStrategy())
            .registerTypeAdapter(ServerInstanceAdaptor.class, new ServerInstanceAdaptor())
            .create();

    public static void initialize() throws IOException {
        Files.createDirectories(Platform.get().defaultMimoDirectory);
        LoaderInstaller.initialize();
    }

    public static Path getInstanceContainerDirectory() {
        return Platform.get().defaultMimoDirectory.resolve("servers");
    }

    public static String sendSimpleHttpRequest(String stringURL) throws IOException {
        URL url = URL.of(URI.create(stringURL), null);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(2000);
        connection.setUseCaches(false);
        connection.connect();

        InputStream inputStream = connection.getInputStream();
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;

            while ((line = reader.readLine()) != null)
                stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    public static void download(String stringURL, Path path) throws IOException {
        URL url = URL.of(URI.create(stringURL), null);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(2000);
        connection.setUseCaches(false);
        connection.connect();

        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(path.toFile())) {
            int read;
            byte[] buffer = new byte[4096];
            while ((read = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, read);
        }
    }
}
