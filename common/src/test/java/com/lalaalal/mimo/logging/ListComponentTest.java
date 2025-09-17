package com.lalaalal.mimo.logging;

import com.lalaalal.mimo.Mimo;
import org.junit.jupiter.api.Test;

class ListComponentTest {
    @Test
    void testListComponent() {
        Component component = ListComponent.unordered("ABC", "DE\nF", "GHI");
        Mimo.LOGGER.info(component);
    }

}