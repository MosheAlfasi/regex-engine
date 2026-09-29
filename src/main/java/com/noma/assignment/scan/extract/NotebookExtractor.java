package com.noma.assignment.scan.extract;

import java.util.ArrayList;
import java.util.List;

import com.noma.assignment.common.exceptions.AppException;
import com.noma.assignment.common.exceptions.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Extracts the {@code source} of every cell of a Jupyter notebook (nbformat v4); outputs are ignored.
 * Per the spec, {@code source} is either a string or a list of strings (lines).
 */
@Component
@RequiredArgsConstructor
public class NotebookExtractor implements ContentExtractor {

    private final JsonMapper jsonMapper;

    @Override
    public boolean supports(ContentType contentType) {
        return contentType == ContentType.NOTEBOOK;
    }

    @Override
    public List<String> extract(String content) {
        JsonNode cells = parse(content).path("cells");
        if (!cells.isArray()) {
            throw new AppException(ErrorType.INVALID_NOTEBOOK, "missing 'cells' array");
        }
        List<String> sources = new ArrayList<>();
        for (JsonNode cell : cells) {
            JsonNode source = cell.path("source");
            if (source.isArray()) {
                StringBuilder sb = new StringBuilder();
                source.forEach(line -> sb.append(line.asString()));
                sources.add(sb.toString());
            } else if (source.isString()) {
                sources.add(source.asString());
            }
        }
        return sources;
    }

    private JsonNode parse(String content) {
        try {
            return jsonMapper.readTree(content);
        } catch (JacksonException e) {
            throw new AppException(ErrorType.INVALID_NOTEBOOK, e, "malformed JSON");
        }
    }
}
