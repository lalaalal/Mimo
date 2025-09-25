package com.lalaalal.mimo.data;

import com.lalaalal.mimo.ServerInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentFilter {
    private final Map<String, List<String>> filters = new HashMap<>();

    public static ContentFilter of(ServerInstance instance) {
        return new ContentFilter()
                .add("versions", instance.version)
                .add("categories", instance.loader.type())
                .add("server_side", "optional", "required");
    }

    public static ContentFilter of(String key, Object value) {
        return new ContentFilter()
                .add(key, value);
    }

    public ContentFilter add(String key, Object... values) {
        for (Object value : values)
            add(key, Operator.EQUAL, value);
        return this;
    }

    public ContentFilter add(String key, Operator operator, Object value) {
        if (!filters.containsKey(key))
            filters.put(key, new ArrayList<>());
        filters.get(key).add(key + operator + value.toString());

        return this;
    }

    @Override
    public String toString() {
        return "[" + String.join(",", filters.values()
                .stream()
                .map(array -> "[" + String.join(",", array
                        .stream()
                        .map("\"%s\""::formatted)
                        .toList()
                ) + "]")
                .toList()
        ) + "]";
    }

    public enum Operator {
        EQUAL(":"), BIGGER_EQUAL(">="), LOWER_EQUAL("<="), BIGGER(">"), LOWER("<");

        public final String literal;

        Operator(String literal) {
            this.literal = literal;
        }

        @Override
        public String toString() {
            return literal;
        }
    }
}
