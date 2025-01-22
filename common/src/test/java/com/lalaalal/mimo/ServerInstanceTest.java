package com.lalaalal.mimo;

import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ContentInstance;
import com.lalaalal.mimo.data.ProjectType;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ServerInstanceTest {

    @Test
    void addContent() throws IOException {
        MimoTest.TEST_INSTANCE.addContent(new Content(ProjectType.MOD, "fabric-api"));
        for (ContentInstance content : MimoTest.TEST_INSTANCE.getContents()) {
            System.out.println(content);
        }
        MimoTest.TEST_INSTANCE.downloadContents();
    }

    @Test
    void downloadContents() {
    }
}