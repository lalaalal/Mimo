package com.lalaalal.mimo.logging;

import com.lalaalal.mimo.Mimo;
import org.junit.jupiter.api.Test;

class ListMessageComponentTest {
    @Test
    void testListComponent() {
        MessageComponent component = ListMessageComponent.unordered("ABC", "DE\nF", "GHI");
        Mimo.LOGGER.info(component);
    }

}