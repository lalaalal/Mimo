package com.lalaalal.mimo.contentprovider;

import com.lalaalal.mimo.curseforge.CurseForgeContentProvider;
import com.lalaalal.mimo.modrinth.ModrinthContentProvider;
import com.lalaalal.mimo.registry.Registries;
import com.lalaalal.mimo.registry.RegistryItem;

public class ContentProviders {
    public static final RegistryItem<ContentProvider> MODRINTH = Registries.CONTENT_PROVIDERS.registerAndGetItem("modrinth", ModrinthContentProvider.INSTANCE);
    public static final RegistryItem<ContentProvider> CURSEFORGE = Registries.CONTENT_PROVIDERS.registerAndGetItem("curseforge", CurseForgeContentProvider.INSTANCE);
    public static final RegistryItem<ContentProvider> CUSTOM =  Registries.CONTENT_PROVIDERS.registerAndGetItem("custom", CustomContentProvider.INSTANCE);

    public static void initialize() {

    }
}
