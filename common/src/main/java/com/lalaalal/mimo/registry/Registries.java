package com.lalaalal.mimo.registry;

import com.lalaalal.mimo.contentprovider.ContentProvider;
import com.lalaalal.mimo.loader.ServerLauncher;

public class Registries {
    public static final Registry<Registry<?>> ROOT = Registry.ROOT;
    public static final Registry<ContentProvider> CONTENT_PROVIDERS = Registry.create("content_providers");
    public static final Registry<ServerLauncher> SERVER_LAUNCHERS = Registry.create("server_launchers");

    public static void initialize() {

    }
}
