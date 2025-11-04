package com.lalaalal.mimo;

import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.data.ProjectType;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.modrinth.ModrinthContentProvider;

import java.io.IOException;
import java.util.List;

public class MimoTest {
    public static final MinecraftVersion TEST_MINECRAFT_VERSION = MinecraftVersion.of(21, 1);
    public static final String TEST_NEOFORGE_VERSION = "21.1.209";
    public static final ServerInstance TEST_INSTANCE = createServerInstance();

    public static final String CONTENT_SLUG = "fabric-api";
    public static final String CONTENT_ID = "P7dR8mSH";
    public static final Content MODRINTH_TEST_CONTENT = new Content(ProjectType.MOD, List.of(Loader.Type.FABRIC), ModrinthContentProvider.INSTANCE, CONTENT_ID, CONTENT_SLUG);
    public static final Content.Version TEST_CONTENT_VERSION = new Content.Version("biIRIp2X", "545047b690a33a593aa999c1fe5e2216e0493d36", "https://cdn.modrinth.com/data/P7dR8mSH/versions/biIRIp2X/fabric-api-0.114.0%2B1.21.1.jar", "fabric-api-0.114.0+1.21.1.jar", List.of());

    private static ServerInstance createServerInstance() {
        try {
            return new ServerInstance("test", new Loader(Loader.Type.FABRIC, "0.16.10"), TEST_MINECRAFT_VERSION);
        } catch (IOException exception) {
            throw new RuntimeException();
        }
    }
}
