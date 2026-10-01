package com.example.newsService.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class NewsDateTimeParser {

    private NewsDateTimeParser() {
    }

    public static LocalDateTime parseOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException dateTimeException) {
            try {
                return LocalDate.parse(value).atStartOfDay();
            } catch (DateTimeParseException dateException) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "publishedAt must be an ISO date (yyyy-MM-dd) or date-time",
                    dateException
                );
            }
        }
    }
}
