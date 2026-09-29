package com.noma.assignment.common.exceptions;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ErrorType type;

    public AppException(ErrorType type, Object... args) {
        this(type, null, args);
    }

    public AppException(ErrorType type, Throwable cause, Object... args) {
        super(type.format(args), cause);
        this.type = type;
    }
}
