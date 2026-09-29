package com.noma.assignment.scan.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/** Where to fetch the content from. The JSON {@code "type"} field selects the concrete source. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HttpSource.class, name = "HTTP"),
        @JsonSubTypes.Type(value = FileSource.class, name = "FILE")
})
public sealed interface SourceDTO permits HttpSource, FileSource {
}
