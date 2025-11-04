package com.lalaalal.mimo.curseforge;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.content_provider.ContentProvider;
import com.lalaalal.mimo.content_provider.Request;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.logging.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CurseForgeResponseParserTest {
    private final CurseForgeRequestFactory factory = new CurseForgeRequestFactory();
    private final CurseForgeResponseParser parser = new CurseForgeResponseParser();
    private final ContentProvider contentProvider = CurseForgeContentProvider.INSTANCE;

    @BeforeAll
    static void setup() {
        Mimo.LOGGER.setLevel(Level.VERBOSE);
    }

    @Test
    void testParseProject() {
        Request request = factory.searchSlug("apotheosis");
        Content content = contentProvider.get(request, response -> parser.parseContent(MinecraftVersion.of(21, 1), Loader.Type.NEOFORGE, response));
        List<Content.Version> versions = contentProvider.get(request, response -> parser.parseProjectVersionList(MinecraftVersion.of(21, 1), Loader.Type.NEOFORGE, response));
        Mimo.LOGGER.info(content.toString());
        Mimo.LOGGER.info(versions.toString());
    }

    @Test
    void testParseProjectVersions() {
        Request request = factory.mod("313970");
        List<Content.Version> versions = contentProvider.get(request, response -> parser.parseProjectVersionList(MinecraftVersion.of(21, 1), Loader.Type.NEOFORGE, response));
        Mimo.LOGGER.info(versions.toString());
    }
}
