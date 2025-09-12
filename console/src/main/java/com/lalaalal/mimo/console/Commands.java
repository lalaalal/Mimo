package com.lalaalal.mimo.console;

import com.lalaalal.mimo.Mimo;

import java.util.Optional;

public class Commands {
    public static Command register(String name, Command command) {
        return Registries.COMMANDS.register(name, command);
    }

    public static Optional<Command> get(String name) {
        return Optional.ofNullable(Registries.COMMANDS.get(name));
    }

    public static final Command INSTALL = register("install",
            Command.simple(ArgumentParsers.LOADER_TYPE, ArgumentParsers.STRING, ArgumentParsers.MINECRAFT_VERSION, ArgumentParsers.STRING)
                    .action(Mimo::install)
                    .message("Minecraft {} with loader {} installed.", 2, 0)
                    .build()
    );

    public static final Command LOAD_INSTANCE = register("load",
            Command.simple(ArgumentParsers.STRING)
                    .action(Mimo::load)
                    .build()
    );

    public static final Command ADD_MOD = register("add_mod",
            Command.simple(ArgumentParsers.STRING)
                    .action(Mimo::addMod)
                    .build()
    );

    public static final Command REMOVE_MOD = register("remove_mod",
            Command.simple(ArgumentParsers.STRING)
                    .action(Mimo::removeMod)
                    .build()
    );

    public static final Command UPDATE = register("update",
            Command.simple()
                    .action(Mimo::updateMods)
                    .build()
    );

    public static final Command LIST_MODS = register("list_mods",
            Command.simple()
                    .action(MimoConsole::listMods)
                    .message("")
                    .build()
    );

    public static final Command LAUNCH = register("launch",
            Command.simple()
                    .action(MimoConsole::launchServer)
                    .build()
    );

    public static void initialize() {

    }
}
