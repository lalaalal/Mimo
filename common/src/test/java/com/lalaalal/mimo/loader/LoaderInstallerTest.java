package com.lalaalal.mimo.loader;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.MimoTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class LoaderInstallerTest {
    @BeforeAll
    static void initialize() throws IOException {
        Mimo.initialize();
    }

    @Test
    void testLoaderVersions() {
        LoaderInstaller fabricInstaller = LoaderInstaller.get(Loader.Type.FABRIC);
        for (String availableVersion : fabricInstaller.getAvailableVersions(MimoTest.TEST_MINECRAFT_VERSION)) {
            System.out.println(availableVersion);
        }
    }

    @Test
    void install() throws IOException {
        LoaderInstaller fabricInstaller = LoaderInstaller.get(Loader.Type.FABRIC);
        fabricInstaller.install("me", MimoTest.TEST_MINECRAFT_VERSION, "0.16.10");
    }
}