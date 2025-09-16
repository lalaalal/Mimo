package com.lalaalal.mimo;

import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.loader.LoaderInstaller;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class InstanceLoaderTest {
    @Disabled
    @Test
    void install() throws IOException, InterruptedException {
        Mimo.initialize();
        LoaderInstaller installer = LoaderInstaller.get(Loader.Type.FABRIC);
        ServerInstance serverInstance = installer.install("test", MimoTest.TEST_MINECRAFT_VERSION, "0.16.10");
    }

    @Disabled
    @Test
    void loadServerFromDirectory() throws IOException {
        ServerInstance serverInstance = ServerInstance.from(Mimo.getInstanceContainerDirectory().resolve("test"));
        serverInstance.save();
    }
}