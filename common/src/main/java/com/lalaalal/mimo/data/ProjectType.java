package com.lalaalal.mimo.data;

import java.util.Optional;

public enum ProjectType {
    MOD("mods"),
    DATAPACK("world/datapacks");

    public final String path;

    ProjectType(String path) {
        this.path = path;
    }

    public static ProjectType get(String name) {
        return valueOf(name.toUpperCase());
    }

    public static Optional<ProjectType> getOptional(String name) {
        return Optional.ofNullable(get(name));
    }
}
