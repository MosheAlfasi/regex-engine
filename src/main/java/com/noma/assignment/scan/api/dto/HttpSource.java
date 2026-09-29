package com.noma.assignment.scan.api.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

/** @param headers extra request headers, e.g. Authorization; optional */
public record HttpSource(@NotBlank String url, Map<String, String> headers) implements SourceDTO {

    public HttpSource {
        headers = headers == null ? Map.of() : headers;
    }
}
