package com.lalaalal.mimo.loader;

import com.lalaalal.mimo.registry.Registries;
import com.lalaalal.mimo.registry.RegistryItem;

import java.io.IOException;
import java.nio.file.Path;

public interface ServerLauncher {
    RegistryItem<ServerLauncher> FABRIC = Registries.SERVER_LAUNCHERS.registerAndGetItem(
            "fabric",
            launcherFile -> new ProcessBuilder("java", "-jar", launcherFile, "nogui")
    );

    RegistryItem<ServerLauncher> FORGE_LIKE = Registries.SERVER_LAUNCHERS.registerAndGetItem(
            "forge_like",
            launcherFile -> new ProcessBuilder("./run.sh", "nogui")
    );

    static void initialize() {

    }

    ProcessBuilder createBuilder(String launcherFile);

    default Process launch(String launcherFile, Path workingDirectory) throws IOException {
        ProcessBuilder processBuilder = createBuilder(launcherFile);
        processBuilder.directory(workingDirectory.toFile());
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        return processBuilder.start();
    }
}
