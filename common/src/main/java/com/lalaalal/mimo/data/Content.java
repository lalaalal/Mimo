package com.lalaalal.mimo.data;

import com.lalaalal.mimo.loader.Loader;

import java.util.List;
import java.util.Objects;

public record Content(ProjectType type, List<Loader.Type> loaders, String id, String slug) {
    private static List<Loader.Type> determineLoader(ProjectType type, Loader.Type loader) {
        if (type == ProjectType.DATAPACK)
            return List.of(Loader.Type.DATAPACK);
        return List.of(loader);
    }

    public Content(ProjectType type, Loader.Type loader, String id, String slug) {
        this(type, determineLoader(type, loader), id, slug);
    }

    public Loader.Type loader() {
        if (loaders.isEmpty())
            throw new IllegalStateException("Content has no loaders");
        return loaders.getFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Content content)) return false;

        return type == content.type && Objects.equals(id, content.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

    public record Version(String versionId, String hash, String url, String fileName, List<Dependency> dependencies) {
    }

    public record Dependency(String id, boolean required) {

    }
}
