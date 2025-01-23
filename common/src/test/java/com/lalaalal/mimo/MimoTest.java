package com.lalaalal.mimo;

import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.data.ProjectType;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.loader.LoaderInstaller;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MimoTest {
    public static final String CONTENT_SLUG = "fabric-api";
    public static final MinecraftVersion TEST_MINECRAFT_VERSION = MinecraftVersion.of(21, 1);
    public static final ServerInstance TEST_INSTANCE = createServerInstance();
    public static final Content.Version TEST_CONTENT_VERSION = new Content.Version("9YVrKY0Z", "41594bd81f1e60e364f76b2e2bfca10cfdcf91bd", "https://cdn.modrinth.com/data/P7dR8mSH/versions/biIRIp2X/fabric-api-0.114.0%2B1.21.1.jar", "fabric-api-0.114.0+1.21.1.jar");

    private static ServerInstance createServerInstance() {
        try {
            return new ServerInstance("test", new Loader(Loader.Type.FABRIC, "0.16.10"), TEST_MINECRAFT_VERSION);
        } catch (IOException exception) {
            throw new RuntimeException();
        }
    }

    @Test
    void test() throws IOException {
        Mimo.initialize();
        LoaderInstaller installer = LoaderInstaller.get(Loader.Type.FABRIC);
        ServerInstance serverInstance = installer.install("nice", TEST_MINECRAFT_VERSION, "0.16.10");
        serverInstance.addContent(new Content(ProjectType.MOD, "betterend"));
        serverInstance.downloadContents();
    }
}
