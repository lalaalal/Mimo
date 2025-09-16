package com.lalaalal.mimo.console.argument;

import com.lalaalal.mimo.console.Registries;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.data.ProjectType;
import com.lalaalal.mimo.loader.Loader;

import java.util.function.Function;

public class ArgumentParsers {
    public static final ArgumentParser<String> STRING = register(
            "string", ArgumentParser.of(String.class, Function.identity())
    );

    public static final ArgumentParser<Integer> INTEGER = register(
            "integer", ArgumentParser.of(Integer.class, Integer::parseInt)
    );

    public static final ArgumentParser<Long> LONG = register(
            "long", ArgumentParser.of(Long.class, Long::parseLong)
    );

    public static final ArgumentParser<Float> FLOAT = register(
            "float", ArgumentParser.of(Float.class, Float::parseFloat)
    );

    public static final ArgumentParser<Double> DOUBLE = register(
            "double", ArgumentParser.of(Double.class, Double::parseDouble)
    );

    public static final ArgumentParser<MinecraftVersion> MINECRAFT_VERSION = register(
            "minecraft_version", ArgumentParser.of(MinecraftVersion.class, MinecraftVersion::of)
    );

    public static final ArgumentParser<Loader.Type> LOADER_TYPE = register(
            "loader_type", ArgumentParser.of(Loader.Type.class, Loader.Type::get)
    );

    public static final ArgumentParser<ProjectType> PROJECT_TYPE = register(
            "project_type", ArgumentParser.of(ProjectType.class, ProjectType::get)
    );

    public static <T> ArgumentParser<T> register(String name, ArgumentParser<T> parser) {
        Registries.ARGUMENT_PARSERS.register(name, parser);
        return parser;
    }

    public static void initialize() {

    }
}
