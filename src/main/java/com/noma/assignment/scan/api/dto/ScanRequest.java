package com.noma.assignment.scan.api.dto;

import java.util.List;

import com.noma.assignment.scan.extract.ContentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ScanRequest(
        @Valid @NotNull SourceDTO source,
        @NotNull ContentType contentType,
        @NotEmpty List<@NotBlank String> patterns) {
}
