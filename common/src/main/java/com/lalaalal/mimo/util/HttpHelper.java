package com.lalaalal.mimo.util;

import com.lalaalal.mimo.Mimo;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

public class HttpHelper {
    public static String sendSimpleHttpRequest(String stringURL) throws IOException {
        Mimo.LOGGER.debug("Connecting to \"%s\"".formatted(stringURL));
        URL url = URL.of(URI.create(stringURL), null);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(2000);
        connection.setUseCaches(false);
        connection.connect();

        Mimo.LOGGER.debug("Reading response");
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
        Mimo.LOGGER.debug("Connecting to \"%s\"".formatted(stringURL));
        URL url = URL.of(URI.create(stringURL), null);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(2000);
        connection.setUseCaches(false);
        connection.connect();

        Mimo.LOGGER.debug("Downloading file to \"%s\"".formatted(path));
        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(path.toFile())) {
            int read;
            byte[] buffer = new byte[4096];
            while ((read = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, read);
        }
    }
}
