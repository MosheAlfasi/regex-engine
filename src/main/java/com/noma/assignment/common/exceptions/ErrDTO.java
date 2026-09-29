package com.noma.assignment.common.exceptions;

public record ErrDTO(ErrorType type, String message) {

    public static ErrDTO of(ErrorType type, Object... args) {
        return new ErrDTO(type, type.format(args));
    }
}
