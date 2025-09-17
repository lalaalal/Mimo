package com.lalaalal.mimo.loader;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.MimoTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class NeoForgeInstallerTest {
    @BeforeAll
    static void initialize() throws IOException {
        Mimo.initialize();
    }

    @Test
    void testLoadVersions() {
        LoaderInstaller installer = LoaderInstaller.get(Loader.Type.NEOFORGE);
        List<String> versions = installer.getAvailableVersions(MimoTest.TEST_MINECRAFT_VERSION);
        versions.forEach(Mimo.LOGGER::info);
    }

    @Test
    void testValidVersion() {
        LoaderInstaller installer = LoaderInstaller.get(Loader.Type.NEOFORGE);
        Assertions.assertTrue(installer.isValidVersion(MimoTest.TEST_MINECRAFT_VERSION, MimoTest.TEST_NEOFORGE_VERSION));
        Assertions.assertFalse(installer.isValidVersion(MimoTest.TEST_MINECRAFT_VERSION, "21.0.1"));
    }

    @Test
    void testInstall() throws IOException, InterruptedException {
        LoaderInstaller installer = LoaderInstaller.get(Loader.Type.NEOFORGE);
        installer.install("test", MimoTest.TEST_MINECRAFT_VERSION, MimoTest.TEST_NEOFORGE_VERSION);
    }
}