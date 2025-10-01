package com.lalaalal.mimo.modrinth;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.MimoTest;
import com.lalaalal.mimo.data.Content;
import com.lalaalal.mimo.data.ContentFilter;
import com.lalaalal.mimo.logging.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

class ResponseParserTest {
    @BeforeAll
    static void setup() {
        Mimo.LOGGER.setLevel(Level.DEBUG);
    }

    @Test
    void testParseSearchData() {
        Map<String, Content.Detail> contents = ModrinthHelper.get(
                Request.search("Apple Skin", ContentFilter.of(MimoTest.TEST_INSTANCE)),
                ResponseParser::parseSearchData
        );
        System.out.println(contents);
    }
}