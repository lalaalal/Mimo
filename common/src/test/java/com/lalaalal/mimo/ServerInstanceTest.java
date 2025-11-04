package com.lalaalal.mimo;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class ServerInstanceTest {

    @Test
    void addContent() throws IOException {
        MimoTest.TEST_INSTANCE.addContent(MimoTest.MODRINTH_TEST_CONTENT);
        MimoTest.TEST_INSTANCE.get(MimoTest.MODRINTH_TEST_CONTENT)
                .setContentVersion(MimoTest.TEST_CONTENT_VERSION);
        MimoTest.TEST_INSTANCE.downloadContents();
    }

    @Test
    void updateContents() throws IOException {
        ServerInstance instance = Mimo.load("test");
        instance.updateContents();
    }

    @Test
    void downloadContents() {
    }
}