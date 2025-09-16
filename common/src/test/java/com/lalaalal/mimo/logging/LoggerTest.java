package com.lalaalal.mimo.logging;

import com.lalaalal.mimo.Mimo;
import org.junit.jupiter.api.Test;

class LoggerTest {

    @Test
    void log() {
        Mimo.LOGGER.error("This is an error");
        Mimo.LOGGER.warning("This is a warning");
        Mimo.LOGGER.info("This is an info");
        Mimo.LOGGER.debug("This is a debug");
    }
}