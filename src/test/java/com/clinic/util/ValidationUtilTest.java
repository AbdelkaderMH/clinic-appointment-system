package com.clinic.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void testAssertTrueWithValidCondition() {
        assertDoesNotThrow(() -> ValidationUtil.assertTrue(true, "Should not throw"));
    }

    @Test
    void testAssertTrueWithInvalidCondition() {
        String message = "Condition failed";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> ValidationUtil.assertTrue(false, message));
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testAssertTrueWithNullMessage() {
        assertThrows(IllegalArgumentException.class, 
            () -> ValidationUtil.assertTrue(false, null));
    }
}