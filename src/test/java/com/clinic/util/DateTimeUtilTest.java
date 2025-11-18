package com.clinic.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilTest {

    @Test
    void testFormat() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30);
        String formatted = DateTimeUtil.format(dateTime);
        assertEquals("2024-01-15T10:30:00", formatted);
    }

    @Test
    void testFormatWithNullInput() {
        assertThrows(NullPointerException.class, () -> 
            DateTimeUtil.format(null));
    }
}