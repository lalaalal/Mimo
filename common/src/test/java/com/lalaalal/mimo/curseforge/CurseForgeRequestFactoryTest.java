package com.lalaalal.mimo.curseforge;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.content_provider.ContentProvider;
import com.lalaalal.mimo.content_provider.Request;
import com.lalaalal.mimo.logging.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CurseForgeRequestFactoryTest {
    private final CurseForgeRequestFactory factory = new CurseForgeRequestFactory();
    private final ContentProvider contentProvider = CurseForgeContentProvider.INSTANCE;

    @BeforeAll
    static void setup() {
        Mimo.LOGGER.setLevel(Level.VERBOSE);
    }

    @Test
    void testGetGames() {
        Request request = factory.games();
        contentProvider.send(request);
    }

    @Test
    void testGameVersionTypes() {
        Request request = factory.gameVersionTypes();
        contentProvider.send(request);
    }

    @Test
    void testSearchSlug() {
        Request request = factory.searchSlug("fabric-api");
        contentProvider.send(request);
    }
}
