package com.distributedtaskscheduler.dispatch.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

public final class DispatchScope {

    private static final int MAX_LENGTH = 64;
    private static final Pattern PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

    private DispatchScope() {
    }

    public static String toLimiterKey(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return "global";
        }
        String value = headerValue.trim();
        if (value.length() > MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return value;
    }
}
