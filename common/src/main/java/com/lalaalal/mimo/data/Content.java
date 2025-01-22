package com.lalaalal.mimo.data;

public record Content(ProjectType type, String slug) {
    public record Version(String id, String hash, String url, String fileName) {

    }
}
