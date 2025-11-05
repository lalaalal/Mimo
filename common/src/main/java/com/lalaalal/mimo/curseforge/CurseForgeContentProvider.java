package com.lalaalal.mimo.curseforge;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.content_provider.ContentProvider;
import com.lalaalal.mimo.content_provider.Request;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.exception.MessageComponentException;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurseForgeContentProvider extends ContentProvider {
    public static final CurseForgeContentProvider INSTANCE = new CurseForgeContentProvider();

    private static final String API_KEY = "$2a$10$jpKD/dKcrY6DTpDzaTA.U.K4In6RZ38BxAHYmAipcAmrfz/3loMtC";
    private static final String CURSE_FORGE_API_URL = "https://api.curseforge.com/v1";

    private final CurseForgeRequestFactory factory = new CurseForgeRequestFactory();
    private final CurseForgeResponseParser parser = new CurseForgeResponseParser();

    public CurseForgeContentProvider() {
        super("curseforge", CURSE_FORGE_API_URL);
    }

    @Override
    protected void setupPostRequest(HttpsURLConnection connection, Request request) throws IOException {
        super.setupPostRequest(connection, request);
        connection.setRequestProperty("X-Api-Key", API_KEY);
    }

    @Override
    public Content getContentWithSlug(String slug, ServerInstance serverInstance) {
        List<Content> contents = get(factory.searchSlug(slug), response -> parser.parseContents(serverInstance.version, serverInstance.loader.type(), response));
        if (contents.isEmpty())
            throw new MessageComponentException("[%s] No content found for %s".formatted(serverInstance, slug));
        return contents.getFirst();
    }

    @Override
    public Content getContentWithId(String id, ServerInstance serverInstance) {
        return get(factory.mod(id), response -> parser.parseContent(serverInstance.version, serverInstance.loader.type(), response));
    }

    @Override
    public List<Content.Version> getProjectVersions(Content content, ServerInstance serverInstance) {
        return get(factory.files(content.id()), response -> parser.parseProjectVersionList(serverInstance.version, serverInstance.loader.type(), response));
    }

    @Override
    public Content.Version getLatestVersion(Content content, Content.Version version, ServerInstance serverInstance) {
        List<Content.Version> versions = forgetAndGet(factory.files(content.id()), response -> parser.parseProjectVersionList(serverInstance.version, serverInstance.loader.type(), response));
        if (versions.isEmpty()) {
            Mimo.LOGGER.error("[{}] No versions found for {}", serverInstance, content.slug());
            throw new MessageComponentException("Aborted");
        }
        return versions.getFirst();
    }

    @Override
    public Map<String, Content.Detail> search(String name) {
        return Map.of();
    }

    @Override
    public Map<Content, Content.Version> getLatestVersions(Map<String, File> hashes, ServerInstance serverInstance) {
        Map<Content, Content.Version> result = new HashMap<>();
        for (File file : hashes.values()) {
            String fileName = file.getName();
            String regex = "^([^-]+)";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                String name = matcher.group(1);
                String connectedSlug = name.toLowerCase();

                List<Content> contents = get(factory.searchSlug(connectedSlug), response -> parser.parseContents(serverInstance.version, serverInstance.loader.type(), response));
                Optional<Content> optionalContent = contents.stream()
                        .filter(content -> content.slug().toLowerCase().equals(connectedSlug))
                        .findAny();
                if (optionalContent.isEmpty()) {
                    String dividedSlug = name.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();

                    contents = get(factory.searchSlug(dividedSlug), response -> parser.parseContents(serverInstance.version, serverInstance.loader.type(), response));
                    optionalContent = contents.stream()
                            .filter(content -> content.slug().toLowerCase().equals(dividedSlug))
                            .findAny();
                }
                if (optionalContent.isPresent()) {
                    Content content = optionalContent.get();
                    List<Content.Version> versions = getProjectVersions(content, serverInstance);
                    versions.stream()
                            .filter(version -> version.fileName().equals(fileName))
                            .findAny()
                            .ifPresent(version -> result.put(content, version));
                }
            }
        }
        return result;
    }
}
