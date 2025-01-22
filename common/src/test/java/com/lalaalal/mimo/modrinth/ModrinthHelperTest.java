package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.MimoTest;
import com.lalaalal.mimo.data.ContentFilter;
import com.lalaalal.mimo.loader.Loader;
import org.junit.jupiter.api.Test;

class ModrinthHelperTest {
    private static final Callback PRINT_RESPONSE = response -> {
        System.out.println(response.code());
        System.out.println(response.data());
    };

    private static void sendAndPrintResult(Request request) {
        ModrinthHelper.sendRequest(request, ModrinthHelperTest.PRINT_RESPONSE);
    }

    @Test
    void testVersions() {
        sendAndPrintResult(Request.versions(MimoTest.CONTENT_SLUG, MimoTest.TEST_MINECRAFT_VERSION, Loader.Type.FABRIC));
    }

    @Test
    void testSearch() {
        sendAndPrintResult(Request.search("fabric-api", ContentFilter.of(MimoTest.TEST_INSTANCE)));
    }

    @Test
    void testLatestVersion() {
        sendAndPrintResult(Request.latestVersion(MimoTest.TEST_CONTENT_VERSION, MimoTest.TEST_INSTANCE));
    }
}