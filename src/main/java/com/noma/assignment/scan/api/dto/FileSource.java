package com.noma.assignment.scan.api.dto;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.noma.assignment.common.exceptions.AppException;
import com.noma.assignment.common.exceptions.ErrorType;
import jakarta.validation.constraints.NotBlank;

/**
 * @param path     relative to the configured base dir
 * @param encoding charset used to decode the file; optional, defaults to UTF-8
 */
public record FileSource(@NotBlank String path, String encoding) implements SourceDTO {

    /** Common names that Java does not recognize as charset aliases. */
    private static final Map<String, String> ENCODING_ALIASES = Map.of("latin-1", "ISO-8859-1");

    public Charset charset() {
        if (encoding == null || encoding.isBlank()) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(ENCODING_ALIASES.getOrDefault(encoding.trim().toLowerCase(), encoding.trim()));
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorType.INVALID_ARGUMENT, e, "unsupported encoding '" + encoding + "'");
        }
    }
}
