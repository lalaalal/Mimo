package com.lalaalal.mimo.console.argument;

import com.lalaalal.mimo.console.Registries;
import com.lalaalal.mimo.data.MinecraftVersion;
import com.lalaalal.mimo.data.ProjectType;
import com.lalaalal.mimo.loader.Loader;
import com.lalaalal.mimo.logging.Level;

import java.util.function.Function;

@SuppressWarnings("unused")
public class ArgumentParsers {
    public static final ArgumentParser<String> STRING = register(
            ArgumentParser.of("string", String.class, Function.identity())
    );

    public static final ArgumentParser<Boolean> BOOLEAN = register(
            ArgumentParser.of("boolean", Boolean.class, Boolean::parseBoolean)
    );

    public static final ArgumentParser<Integer> INTEGER = register(
            ArgumentParser.of("integer", Integer.class, Integer::parseInt)
    );

    public static final ArgumentParser<Long> LONG = register(
            ArgumentParser.of("long", Long.class, Long::parseLong)
    );

    public static final ArgumentParser<Float> FLOAT = register(
            ArgumentParser.of("float", Float.class, Float::parseFloat)
    );

    public static final ArgumentParser<Double> DOUBLE = register(
            ArgumentParser.of("double", Double.class, Double::parseDouble)
    );

    public static final ArgumentParser<MinecraftVersion> MINECRAFT_VERSION = register(
            ArgumentParser.of("minecraft_version", MinecraftVersion.class, MinecraftVersion::of)
    );

    public static final ArgumentParser<Loader.Type> LOADER_TYPE = register(
            ArgumentParser.of("loader_type", Loader.Type.class, Loader.Type::get)
    );

    public static final ArgumentParser<ProjectType> PROJECT_TYPE = register(
            ArgumentParser.of("project_type", ProjectType.class, ProjectType::get)
    );

    public static final ArgumentParser<Level> LOG_LEVEL = register(
            ArgumentParser.of("log_level", Level.class, Level::get)
    );

    public static <T> ArgumentParser<T> register(ArgumentParser<T> parser) {
        Registries.ARGUMENT_PARSERS.register(parser.name, parser);
        return parser;
    }

    public static void initialize() {

    }
}
