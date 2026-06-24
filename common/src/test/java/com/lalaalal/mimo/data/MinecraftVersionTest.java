package com.lalaalal.mimo.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinecraftVersionTest {

    @Test
    void testVersionParsing() {
        MinecraftVersion version1 = MinecraftVersion.of("1.21.1");
        MinecraftVersion version2 = MinecraftVersion.of("26.1");
        MinecraftVersion version3 = MinecraftVersion.of("24w12a");

        assertEquals(MinecraftVersion.Type.STABLE, version1.type());
        assertEquals(MinecraftVersion.Type.STABLE, version2.type());
        assertEquals(MinecraftVersion.Type.SNAPSHOT, version3.type());
    }
}