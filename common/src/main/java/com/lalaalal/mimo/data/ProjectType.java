package com.lalaalal.mimo.data;

import java.util.Optional;

public enum ProjectType {
    MOD("mods"),
    DATAPACK("world/datapacks");

    public final String path;

    ProjectType(String path) {
        this.path = path;
    }

    public static ProjectType getOrDefault(String name, ProjectType defaultValue) {
        for (ProjectType value : values()) {
            if (value.name().equalsIgnoreCase(name))
                return value;
        }
        return defaultValue;
    }

    public static ProjectType get(String name) {
        ProjectType value = getOrDefault(name, null);
        if (value == null)
            throw new IllegalArgumentException("Unknown ProjectType : " + name);
        return value;
    }

    public static Optional<ProjectType> getOptional(String name) {
        return Optional.ofNullable(getOrDefault(name, null));
    }
}
