package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.MimoTest;
import com.lalaalal.mimo.data.ContentFilter;
import com.lalaalal.mimo.loader.Loader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

class RequestTest {
    @Test
    void testPathParamMaker() {
        Request request = Request.project("fabric-api");
        Assertions.assertEquals("project/fabric-api", request.createQuery());
    }

    @Test
    void testDependenciesPathParamMaker() {
        Request request = Request.dependencies("fabric-api");
        Assertions.assertEquals("project/fabric-api/dependencies", request.createQuery());
    }

    @Test
    void testQueryParamMaker() {
        Request request = Request.search("fabric-api",
                ContentFilter.of(MimoTest.TEST_INSTANCE)
        );
        System.out.println(URLDecoder.decode(request.createQuery(), StandardCharsets.UTF_8));
    }

    @Test
    void testVersionParamMaker() {
        Request request = Request.projectVersions("fabric-api", MimoTest.TEST_MINECRAFT_VERSION, Loader.Type.FABRIC);
        System.out.println(request.createQuery());
    }
}