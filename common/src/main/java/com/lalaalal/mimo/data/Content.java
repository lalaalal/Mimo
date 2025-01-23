package com.lalaalal.mimo.data;

import java.util.Objects;

public record Content(ProjectType type, String id, String slug) {
    public Content(ProjectType type, String id) {
        this(type, id, "");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Content content)) return false;
        return Objects.equals(id, content.id) && type == content.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

    public record Version(String versionId, String hash, String url, String fileName) {
    }
}
