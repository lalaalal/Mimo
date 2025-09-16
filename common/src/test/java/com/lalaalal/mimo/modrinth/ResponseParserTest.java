package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.MimoTest;
import com.lalaalal.mimo.data.ContentFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

class ResponseParserTest {
    @Test
    void testParseSearchData() {
        List<String> contents = ModrinthHelper.get(
                Request.search("veinminer", ContentFilter.of(MimoTest.TEST_INSTANCE)),
                ResponseParser::parseSearchData
        );
        System.out.println(contents);
    }
}