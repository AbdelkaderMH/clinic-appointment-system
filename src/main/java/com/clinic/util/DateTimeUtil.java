package com.clinic.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility methods for working with dates and times.
 */
public final class DateTimeUtil {
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateTimeUtil() {
    }

    /**
     * Format the given {@link LocalDateTime} as an ISO‑8601 string.
     *
     * @param time the date/time to format
     * @return formatted string
     */
    public static String format(LocalDateTime time) {
        return ISO_DATE_TIME.format(time);
    }
}