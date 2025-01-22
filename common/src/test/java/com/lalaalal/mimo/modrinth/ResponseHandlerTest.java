package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.MimoTest;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ContentFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

class ResponseHandlerTest {

    @Test
    void testResolveSearchData() {
        ModrinthHelper.sendRequest(Request.search("terra", ContentFilter.of(MimoTest.TEST_INSTANCE)), response -> {
            List<Content> contents = ResponseHandler.resolveSearchData(response);
            System.out.println(contents);
        });
    }
}