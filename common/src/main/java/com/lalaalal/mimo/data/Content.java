package com.lalaalal.mimo.data;

import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.modrinth.ModrinthHelper;
import com.lalaalal.mimo.modrinth.Request;
import com.lalaalal.mimo.modrinth.Response;
import com.lalaalal.mimo.modrinth.ResponseParser;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a content that can be loaded by a loader.
 * Content can be created by calling:
 * <pre>{@code
 * Content content = ModrinthHelper.get(Request.project(id_or_slug), ResponseParser::parseProject);
 * }</pre>
 *
 * @param type    {@link ProjectType} of the content
 * @param loaders List of {@link Loader.Type} that can load this content
 * @param id      Content identifier
 * @param slug    Content slug
 * @see ModrinthHelper#get(Request, Function)
 * @see Request#project(String)
 * @see ResponseParser#parseContent(Loader.Type, Response)
 */
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
        public static Version custom(String hash, File file) {
            return new Version(hash.substring(0, 4) + "-custom", hash, "", file.getName(), List.of());
        }

        public boolean isCustom() {
            return versionId.endsWith("custom");
        }
    }

    public record Dependency(String id, boolean required) {

    }

    public record Detail(String title, String description) {

    }
}
