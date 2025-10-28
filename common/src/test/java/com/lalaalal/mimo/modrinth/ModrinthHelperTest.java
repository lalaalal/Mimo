package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.MimoTest;
import com.lalaalal.mimo.data.ContentFilter;
import com.lalaalal.mimo.loader.Loader;
import org.junit.jupiter.api.Test;

class ModrinthHelperTest {
    private static void printResponse(Response response) {
        System.out.println(response.code());
        System.out.println(response.data());
    }

    private static void sendAndPrintResult(Request request) {
        Response response = ModrinthHelper.send(request);
        printResponse(response);
    }

    @Test
    void project() {
        sendAndPrintResult(Request.project("fabric-api"));
    }

    @Test
    void projects() {
        sendAndPrintResult(Request.projects("custom-af36a"));
    }

    @Test
    void version() {
        sendAndPrintResult(Request.version(MimoTest.TEST_CONTENT_VERSION.hash()));
    }

    @Test
    void projectVersions() {
        sendAndPrintResult(Request.projectVersions("betterend", MimoTest.TEST_MINECRAFT_VERSION, Loader.Type.FABRIC));
    }

    @Test
    void versions() {
        sendAndPrintResult(Request.versions(MimoTest.TEST_CONTENT_VERSION.hash(), "825f4340bd9ab67a08130a4167fc6a01777ab075"));
    }

    @Test
    void search() {
        sendAndPrintResult(Request.search("fabric-api", ContentFilter.of(MimoTest.TEST_INSTANCE)));
    }

    @Test
    void latestVersion() {
        sendAndPrintResult(Request.latestVersion(MimoTest.TEST_CONTENT, MimoTest.TEST_CONTENT_VERSION, MimoTest.TEST_INSTANCE));
    }

    @Test
    void dependencies() {
        sendAndPrintResult(Request.dependencies("betterend"));
    }
}