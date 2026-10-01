package com.example.newsService.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class NewsDateTimeParserTest {

    @Test
    void parsesDateOnlyAtStartOfDay() {
        assertEquals(
            LocalDateTime.of(2026, 10, 1, 0, 0),
            NewsDateTimeParser.parseOptional("2026-10-01")
        );
    }

    @Test
    void parsesDateTimeWithoutChangingTime() {
        assertEquals(
            LocalDateTime.of(2026, 10, 1, 20, 56, 54),
            NewsDateTimeParser.parseOptional("2026-10-01T20:56:54")
        );
    }

    @Test
    void permitsMissingOrBlankOptionalDate() {
        assertNull(NewsDateTimeParser.parseOptional(null));
        assertNull(NewsDateTimeParser.parseOptional(" "));
    }

    @Test
    void rejectsUnsupportedDateFormatWithBadRequest() {
        var exception = assertThrows(
            ResponseStatusException.class,
            () -> NewsDateTimeParser.parseOptional("not-a-date")
        );
        assertEquals(400, exception.getStatusCode().value());
    }
}
