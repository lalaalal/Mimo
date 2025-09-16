package com.lalaalal.mimo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.json.MimoExcludeStrategy;
import com.lalaalal.mimo.json.ServerInstanceAdaptor;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.loader.LoaderInstaller;
import com.lalaalal.mimo.logging.Logger;
import com.lalaalal.mimo.modrinth.ModrinthHelper;
import com.lalaalal.mimo.modrinth.Request;
import com.lalaalal.mimo.modrinth.ResponseParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class Mimo {
    private static ServerInstance currentServerInstance = null;

    public static final Gson GSON = new GsonBuilder()
            .addSerializationExclusionStrategy(new MimoExcludeStrategy())
            .addDeserializationExclusionStrategy(new MimoExcludeStrategy())
            .registerTypeAdapter(ServerInstanceAdaptor.class, new ServerInstanceAdaptor())
            .setPrettyPrinting()
            .create();

    public static final Logger LOGGER = Logger.stdout();

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

    public static ServerInstance load(String name) throws IOException {
        return currentServerInstance = ServerInstance.from(getInstanceContainerDirectory().resolve(name));
    }

    public static void install(Loader.Type type, String name, MinecraftVersion minecraftVersion, String loaderVersion) throws IOException, InterruptedException {
        currentServerInstance = LoaderInstaller.get(type).install(name, minecraftVersion, loaderVersion);
    }

    public static Optional<ServerInstance> currentInstance() {
        return Optional.ofNullable(currentServerInstance);
    }

    public static void add(String slug) {
        currentInstance().ifPresent(serverInstance -> {
            Content content = ModrinthHelper.get(Request.project(slug), ResponseParser.contentParser(serverInstance));
            serverInstance.addContent(content);
        });
    }

    public static void removeMod(String slug) throws IOException {
        Optional<ServerInstance> optionalServerInstance = currentInstance();
        if (optionalServerInstance.isPresent()) {
            ServerInstance serverInstance = optionalServerInstance.get();
            Content content = serverInstance.getContents().stream()
                    .map(ContentInstance::content)
                    .filter(_content -> _content.slug().equals(slug) || _content.id().equals(slug))
                    .findAny()
                    .orElseGet(() -> ModrinthHelper.get(Request.project(slug), ResponseParser.contentParser(serverInstance)));
            serverInstance.removeContent(content);
        }
    }

    public static void update() throws IOException {
        Optional<ServerInstance> optional = currentInstance();
        if (optional.isPresent())
            optional.get().updateContents();
    }
}
