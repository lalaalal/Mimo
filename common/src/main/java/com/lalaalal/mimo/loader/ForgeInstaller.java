package com.lalaalal.mimo.loader;

import com.lalaalal.mimo.data.MinecraftVersion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ForgeInstaller extends LoaderInstaller {
    protected ForgeInstaller() {
        super(Loader.Type.FORGE);
    }

    @Override
    public List<String> getAvailableVersions(MinecraftVersion minecraftVersion) {
        return List.of();
    }

    @Override
    public boolean isValidVersion(MinecraftVersion minecraftVersion, String loaderVersion) {
        return false;
    }

    @Override
    protected void processInstall(Path instanceDirectory, MinecraftVersion minecraftVersion, String loaderVersion) throws IOException, InterruptedException {

    }
}
