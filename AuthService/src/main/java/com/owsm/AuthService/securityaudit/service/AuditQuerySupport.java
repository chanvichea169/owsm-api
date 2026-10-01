package com.owsm.AuthService.securityaudit.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

final class AuditQuerySupport {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private AuditQuerySupport() {
    }

    static Pageable pageable(Integer page, Integer size, String sortBy, String direction, List<String> allowedSorts) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? DEFAULT_PAGE_SIZE : size;
        if (pageNumber < 0) {
            throw badRequest("page must be zero or greater");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw badRequest("size must be between 1 and " + MAX_PAGE_SIZE);
        }

        String property = sortBy == null || sortBy.isBlank() ? "occurredAt" : sortBy;
        if (!allowedSorts.contains(property)) {
            throw badRequest("Unsupported sort field");
        }
        String order = direction == null || direction.isBlank() ? "DESC" : direction;
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(order);
        } catch (IllegalArgumentException ex) {
            throw badRequest("direction must be ASC or DESC");
        }

        Sort sort = Sort.by(sortDirection, property);
        if (!"id".equals(property)) {
            sort = sort.and(Sort.by(Sort.Direction.DESC, "id"));
        }
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    static Instant dateBound(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                LocalDate date = LocalDate.parse(value);
                return endOfDay
                        ? date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1)
                        : date.atStartOfDay().toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ex) {
                throw badRequest("dateFrom and dateTo must be ISO-8601 timestamps or dates");
            }
        }
    }

    static void validateDateRange(Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw badRequest("dateFrom must not be after dateTo");
        }
    }

    static InetAddress ipAddress(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String candidate = value.trim();
        boolean ipv4 = candidate.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}");
        boolean ipv6 = candidate.indexOf(':') >= 0 && !candidate.contains("%");
        if (!ipv4 && !ipv6) {
            throw badRequest("ipAddress must be an IPv4 or IPv6 literal");
        }
        try {
            InetAddress address = InetAddress.getByName(candidate);
            if ((ipv4 && address.getAddress().length != 4) || (ipv6 && address.getAddress().length != 16)) {
                throw badRequest("ipAddress must be an IPv4 or IPv6 literal");
            }
            return address;
        } catch (UnknownHostException ex) {
            throw badRequest("ipAddress must be an IPv4 or IPv6 literal");
        }
    }

    static <E extends Enum<E>> E enumValue(Class<E> enumClass, String value, String name) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw badRequest("Invalid " + name);
        }
    }

    static String normalized(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
