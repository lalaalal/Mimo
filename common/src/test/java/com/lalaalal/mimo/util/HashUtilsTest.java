package com.lalaalal.mimo.util;

import com.lalaalal.mimo.Mimo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

class HashUtilsTest {

    @Test
    void hashFile() throws IOException {
        Path path = Mimo.getInstanceContainerDirectory().resolve("nice/mods/fabric-api-0.115.0+1.21.1.jar");
        System.out.println(HashUtils.hashFile(path));
    }
}