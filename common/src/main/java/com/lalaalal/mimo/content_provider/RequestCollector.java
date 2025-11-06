package com.lalaalal.mimo.content_provider;

import com.lalaalal.mimo.ContentInstance;
import com.lalaalal.mimo.ServerInstance;
import com.lalaalal.mimo.data.Content;

import java.util.*;

public class RequestCollector {
    public static ContentProviderAction<Content.Version> multipleLatestVersion(ServerInstance serverInstance) {
        return (contentProvider, contents) -> contentProvider.getMultipleLatestVersion(
                contents, serverInstance
        );
    }

    private final Map<ContentProvider, List<ContentInstance>> contentsByContentProvider = new HashMap<>();

    public void add(ContentInstance contentInstance) {
        contentsByContentProvider.computeIfAbsent(contentInstance.content().provider(), key -> new ArrayList<>())
                .add(contentInstance);
    }

    public void remove(ContentInstance contentInstance) {
        contentsByContentProvider.get(contentInstance.content().provider()).remove(contentInstance);
    }

    public <T> Distributor<T> submit(Type type, ContentProviderAction<T> action) {
        Map<String, T> result = new HashMap<>();
        contentsByContentProvider.forEach((contentProvider, contents) -> {
            result.putAll(action.apply(contentProvider, contents));
        });
        return new Distributor<>(type, result);
    }

    public enum Type {
        MULTIPLE_LATEST_VERSION;
    }

    public interface ContentProviderAction<T> {
        Map<String, T> apply(ContentProvider contentProvider, List<ContentInstance> contents);
    }

    public static final class Distributor<T> {
        private final Type type;
        private final Map<String, T> result;

        private Distributor(Type type, Map<String, T> result) {
            this.type = type;
            this.result = result;
        }

        public Optional<T> getResult(String id) {
            return Optional.ofNullable(result.get(id));
        }

        public boolean is(Type type) {
            return this.type == type;
        }
    }
}
