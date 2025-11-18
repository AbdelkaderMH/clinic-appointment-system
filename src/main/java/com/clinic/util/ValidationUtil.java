package com.clinic.util;

/**
 * Utility methods for common validation checks.
 */
public final class ValidationUtil {
    private ValidationUtil() {
    }

    /**
     * Assert that the given condition is true.  If the condition is
     * false, throw an {@link IllegalArgumentException} with the supplied
     * message.
     *
     * @param condition expression to check
     * @param message   error message if the condition is false
     */
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}