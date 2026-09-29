package com.noma.assignment.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    INVALID_ARGUMENT("Invalid argument: %s", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("Validation failed: %s", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST("Malformed request body", HttpStatus.BAD_REQUEST),
    NOT_FOUND("%s not found: %s", HttpStatus.NOT_FOUND),
    INVALID_PATTERN("Invalid regex pattern: %s", HttpStatus.BAD_REQUEST),
    INVALID_NOTEBOOK("Invalid notebook: %s", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_SOURCE("Unsupported %s: %s", HttpStatus.BAD_REQUEST),
    CONTENT_TOO_LARGE("Content exceeds the limit of %s bytes", HttpStatus.CONTENT_TOO_LARGE),
    FETCH_FAILED("Failed to fetch %s", HttpStatus.BAD_GATEWAY),
    EXTRACTION_FAILED("Failed to extract %s content: %s", HttpStatus.UNPROCESSABLE_CONTENT),
    INTERNAL_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String msgTemplate;
    private final HttpStatus httpStatus;

    public String format(Object... args) {
        return String.format(msgTemplate, args);
    }
}
