package com.lalaalal.mimo.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class MimoExcludeStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        GsonExcludeStrategy typeStrategy = fieldAttributes.getDeclaringClass().getAnnotation(GsonExcludeStrategy.class);
        if (typeStrategy == null)
            return false;

        GsonField fieldStrategy = fieldAttributes.getAnnotation(GsonField.class);
        if (typeStrategy.value() == TypeStrategy.INCLUDE_MARKED)
            return fieldStrategy == null || fieldStrategy.value() == FieldStrategy.EXCLUDE;
        return fieldStrategy != null && fieldStrategy.value() == FieldStrategy.EXCLUDE;
    }

    @Override
    public boolean shouldSkipClass(Class<?> type) {
        return false;
    }
}
