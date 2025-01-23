package com.lalaalal.mimo;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class InstanceLoaderTest {

    @Test
    void loadServerFromDirectory() throws IOException {
        ServerInstance serverInstance = ServerInstance.from(Mimo.getInstanceContainerDirectory().resolve("nice"));
        for (ContentInstance contentInstance : serverInstance.getContents()) {
            System.out.println(contentInstance.content);
        }
    }
}